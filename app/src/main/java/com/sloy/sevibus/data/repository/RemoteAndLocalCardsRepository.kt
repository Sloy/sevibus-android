package com.sloy.sevibus.data.repository

import com.sloy.sevibus.data.api.SevibusApi
import com.sloy.sevibus.data.api.SevibusUserApi
import com.sloy.sevibus.data.api.model.CardInfoDto
import com.sloy.sevibus.data.api.model.CardTransactionDto
import com.sloy.sevibus.data.database.SevibusDao
import com.sloy.sevibus.data.database.fromEntity
import com.sloy.sevibus.data.database.toDto
import com.sloy.sevibus.data.database.toEntity
import com.sloy.sevibus.domain.model.CardId
import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.domain.model.CardTransaction
import com.sloy.sevibus.domain.model.Line
import com.sloy.sevibus.domain.model.toSummary
import com.sloy.sevibus.domain.repository.CardsRepository
import com.sloy.sevibus.domain.repository.LineRepository
import com.sloy.sevibus.infrastructure.SevLogger
import com.sloy.sevibus.infrastructure.session.SessionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.time.LocalDateTime

class RemoteAndLocalCardsRepository(
    private val sevibusDao: SevibusDao,
    private val api: SevibusApi,
    private val userApi: SevibusUserApi,
    private val lineRepository: LineRepository,
    private val sessionService: SessionService,
) : CardsRepository {

    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()

    init {
        sessionService.observeCurrentUser()
            .map { it != null }
            .distinctUntilChanged()
            .onEach { isLogged ->
                if (isLogged) {
                    syncWithServer()
                } else {
                    updateAnonymousCards()
                }
            }
            .catch { SevLogger.logW(it, "Error syncing cards") }
            .launchIn(backgroundScope)
    }

    override fun observeUserCards(): Flow<List<CardInfo>> {
        return sevibusDao.observeCards()
            .map { it.map { it.fromEntity() } }
    }

    override suspend fun obtainUserCards(): List<CardInfo> {
        return sevibusDao.getCards().map { it.fromEntity() }
    }

    override suspend fun replaceUserCards(cards: List<CardInfo>) {
        val entities = cards.mapIndexed { i, card -> card.toEntity(order = i) }
        sevibusDao.replaceAllCards(entities)
        backgroundScope.launch {
            if (sessionService.isLogged()) {
                runCatching { userApi.replaceUserCards(entities.map { it.toDto() }) }
                    .onFailure { SevLogger.logW(it) }
            }
        }

    }

    override suspend fun checkCard(initialCardId: CardId): CardInfo? {
        return try {
            api.getCardInfo(initialCardId).fromDto()
        } catch (httpError: HttpException) {
            if (httpError.code() == 404) {
                null
            } else {
                throw httpError
            }
        }
    }

    override suspend fun addUserCard(cardResult: CardInfo) {
        val lastOrder = sevibusDao.getCards().maxByOrNull { it.order }?.order ?: -1
        val card = cardResult.toEntity(order = lastOrder + 1)
        sevibusDao.putCard(card)
        backgroundScope.launch {
            if (sessionService.isLogged()) {
                runCatching { userApi.addUpdateUserCard(card.serialNumber, card.toDto()) }
                    .onFailure { SevLogger.logW(it) }
            }
        }

    }

    override suspend fun deleteUserCard(card: CardId) {
        sevibusDao.deleteCard(card)
        backgroundScope.launch {
            if (sessionService.isLogged()) {
                runCatching { userApi.deleteUserCard(card) }
                    .onFailure { SevLogger.logW(it) }
            }
        }
    }

    override suspend fun obtainTransactions(cardId: CardId): List<CardTransaction> {
        val lines = lineRepository.obtainLines()
        return api.getCardTransactions(cardId).mapNotNull { it.fromDto(lines) }
    }

    private suspend fun updateAnonymousCards() {
        val cards = sevibusDao.getCards()
        cards.forEach { card ->
            val remoteCard = checkCard(card.serialNumber)
            if (remoteCard != null) {
                val updated = card.copy(
                    balance = remoteCard.balance,
                    type = remoteCard.type,
                )
                if (updated != card) {
                    sevibusDao.putCard(updated)
                }
            }
        }
    }

    private suspend fun syncWithServer() = withContext(Dispatchers.Default) {
        if (!sessionService.isLogged()) return@withContext
        mutex.withLock {
            val localCards = sevibusDao.getCards()
            val remoteCards = userApi.obtainUserCards()

            val missingFromRemote = localCards.filter { localFavorite ->
                remoteCards.none { it.serialNumber == localFavorite.serialNumber }
            }
            missingFromRemote.forEach { local ->
                userApi.addUpdateUserCard(local.serialNumber, local.toDto())
            }

            sevibusDao.insertAllCards(remoteCards.map { it.toEntity() })
        }
    }

}

private fun CardInfoDto.fromDto(): CardInfo {
    return CardInfo(
        serialNumber = serialNumber,
        code = code,
        type = type,
        balance = balance,
        customName = customName,
    )
}

private fun CardTransactionDto.fromDto(lines: List<Line>): CardTransaction? {
    return when (operation) {
        "validation" -> CardTransaction.Validation(
            serialNumber = serialNumber,
            amount = amount ?: error("Amount is required for validation in card $this"),
            date = runCatching { LocalDateTime.parse(date) }.getOrElse {
                throw IllegalStateException(
                    "Error formatting date $date for card $this",
                    it
                )
            },
            line = lines.find { it.label == lineLabel }?.toSummary()
                ?: error("Line $lineLabel not found for transaction in card $this"),
            bus = busId ?: error("Bus ID is required for validation in card $this"),
            people = people ?: 1,
        )

        "topup" -> CardTransaction.TopUp(
            serialNumber = serialNumber,
            amount = amount ?: error("Amount is required for top-up in card $this"),
            date = runCatching { LocalDateTime.parse(date) }.getOrElse {
                throw IllegalStateException(
                    "Error formatting date $date for card $this",
                    it
                )
            },
        )

        "transfer" -> CardTransaction.Transfer(
            serialNumber = serialNumber,
            date = runCatching { LocalDateTime.parse(date) }.getOrElse {
                throw IllegalStateException(
                    "Error formatting date $date for card $this",
                    it
                )
            },
            line = lines.find { it.label == lineLabel }?.toSummary()
                ?: error("Line $lineLabel not found for transaction in card $this"),
            bus = busId ?: error("Bus ID is required for validation in card $this"),
            people = people ?: 1,
        )

        else -> {
            SevLogger.logW(msg = "Unknown operation $operation in card $this")
            null
        }
    }
}


