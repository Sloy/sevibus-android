package com.sloy.sevibus.domain.repository

import com.sloy.sevibus.domain.model.CardId
import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.domain.model.CardTransaction
import kotlinx.coroutines.flow.Flow

interface CardsRepository {
    fun observeUserCards(): Flow<List<CardInfo>>
    suspend fun obtainUserCards(): List<CardInfo>
    suspend fun replaceUserCards(cards: List<CardInfo>)
    suspend fun checkCard(initialCardId: CardId) : CardInfo?
    suspend fun addUserCard(cardResult: CardInfo)
    suspend fun deleteUserCard(card: CardId)
    suspend fun obtainTransactions(cardId: CardId): List<CardTransaction>
    suspend fun dismissAlertForCards(cardIds: List<CardId>)
    suspend fun clearDismissedAlert(cardId: CardId)
    suspend fun getDismissedCardIds(): List<CardId>
    fun observeDismissedCardIds(): Flow<List<CardId>>
}
