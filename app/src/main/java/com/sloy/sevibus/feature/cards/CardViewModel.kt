package com.sloy.sevibus.feature.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloy.sevibus.domain.model.CardId
import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.domain.repository.CardsRepository
import com.sloy.sevibus.infrastructure.SevLogger
import com.sloy.sevibus.infrastructure.analytics.Analytics
import com.sloy.sevibus.infrastructure.analytics.events.Clicks
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CardViewModel(
    private val cardsRepository: CardsRepository,
    private val analytics: Analytics,
    initialCardId: CardId? = null,
) : ViewModel() {

    val events = MutableSharedFlow<CardsScreenEvent>()
    val newCardState = MutableStateFlow<CardsScreenNewCardState>(CardsScreenNewCardState.InputForm())

    private val isReordering = MutableStateFlow(false)
    private val scrollToCard = MutableStateFlow<CardId?>(initialCardId)


    private val cardsFlow: Flow<List<CardAndTransactions>> = cardsRepository.observeUserCards().transformLatest { userCards ->
        emit(userCards.map { CardAndTransactions(it, TransactionsState.Loading) })
        userCards.map { card ->
            val transactionsState = runCatching { cardsRepository.obtainTransactions(card.serialNumber) }
                .map { if (it.isEmpty()) TransactionsState.Empty else TransactionsState.Loaded(it) }
                .onFailure { SevLogger.logE(it, "Error obtaining transactions for card ${card.serialNumber}") }
                .getOrElse { TransactionsState.Error(it) }
            CardAndTransactions(card, transactionsState)
        }.let { emit(it) }
    }

    val state = combineTransform(cardsFlow, isReordering, scrollToCard) { cardAndTransactions, isReordering, scrollTo ->
        if (cardAndTransactions.isEmpty()) {
            emit(CardsScreenState.Empty)
        } else {
            emit(CardsScreenState.Content(cardAndTransactions, isReordering, scrollTo))
        }
    }.catch { error ->
        SevLogger.logE(error, "Error obtaining user cards")
        CardsScreenState.Error(error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), CardsScreenState.Loading)

    fun onNewCardNumber(serialNumber: String) {
        newCardState.value = CardsScreenNewCardState.InputForm(serialNumber)
        if (serialNumber.length < 12 || !serialNumber.all { it.isDigit() }) return
        val cardId = serialNumber.toLong()


        newCardState.value = CardsScreenNewCardState.CheckingCard(serialNumber)
        viewModelScope.launch {
            runCatching { cardsRepository.checkCard(cardId) }
                .onSuccess { card ->
                    if (card != null) {
                        onNewCardReceived(card)
                    } else {
                        events.emit(CardsScreenEvent.ShowMessage("Tarjeta no encontrada o número inválido"))
                        SevLogger.logW(Exception("Card not found with id $cardId"))
                        newCardState.value = CardsScreenNewCardState.InputForm(serialNumber)
                    }
                }
                .onFailure { error ->
                    events.emit(CardsScreenEvent.ShowMessage("Hubo un error al buscar la tarjeta, inténtalo más tarde"))
                    SevLogger.logE(error)
                    newCardState.value = CardsScreenNewCardState.InputForm(serialNumber)
                }
        }
    }

    private suspend fun onNewCardReceived(card: CardInfo) {
        val existingCards = (state.value as? CardsScreenState.Content)?.cardsAndTransactions?.cards()
        val existingCard = existingCards?.find { it.serialNumber == card.serialNumber }
        if (existingCard != null) {
            scrollToCard.value = existingCard.serialNumber
            events.emit(CardsScreenEvent.ShowMessage("Ya tienes guardada esa tarjeta"))
        } else {
            cardsRepository.addUserCard(card)
            scrollToCard.value = card.serialNumber
        }
        newCardState.value = CardsScreenNewCardState.InputForm()
    }

    fun onTopUpClicked(card: CardInfo) {
        viewModelScope.launch {
            events.emit(CardsScreenEvent.LaunchUri("https://recargas.tussam.es/TPW/Common/index.do?client_id=APPTUSSAM&id_tarjeta=${card.fullSerialNumber}"))
            analytics.track(Clicks.CardTopUpClicked(card.type, card.balance))
        }
    }
    fun onDeleteCard(cardId: CardId) {
        viewModelScope.launch {
            runCatching { cardsRepository.deleteUserCard(cardId) }
                .onSuccess { events.emit(CardsScreenEvent.ShowMessage("Tarjeta eliminada")) }
                .onFailure { error ->
                    events.emit(CardsScreenEvent.ShowMessage("Hubo un error al eliminar la tarjeta, inténtalo más tarde"))
                    SevLogger.logE(error, "Error deleting card")
                }
        }
    }

    fun onStartReordering() {
        with(state.value) {
            isReordering.value = true
        }
    }

    fun onReorderingDone(updatedList: List<CardInfo>) {
        viewModelScope.launch {
            runCatching { cardsRepository.replaceUserCards(updatedList) }
                .onSuccess {
                    isReordering.value = false
                }
                .onFailure { error ->
                    events.emit(CardsScreenEvent.ShowMessage("Hubo un error al reordenar las tarjetas, inténtalo más tarde"))
                    SevLogger.logE(error, "Error updating cards")
                }
        }
    }
}
