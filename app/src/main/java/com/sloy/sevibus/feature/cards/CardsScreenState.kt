package com.sloy.sevibus.feature.cards

import com.sloy.sevibus.domain.model.CardId
import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.domain.model.CardTransaction

sealed interface CardsScreenState {
    data object Empty : CardsScreenState
    data object Loading : CardsScreenState
    data class Content(
        val cardsAndTransactions: List<CardAndTransactions>,
        val isReordering: Boolean = false,
        val scrollToCard: CardId? = null,
    ) : CardsScreenState


    data class Error(val error: Throwable) : CardsScreenState
}

sealed interface TransactionsState {
    data object Loading : TransactionsState
    data object Empty : TransactionsState
    data class Loaded(val transactions: List<CardTransaction>) : TransactionsState
    data class Error(val error: Throwable) : TransactionsState
}

sealed class CardsScreenNewCardState(open val serialNumber: String) {
    data class InputForm(override val serialNumber: String = "") : CardsScreenNewCardState(serialNumber)
    data class CheckingCard(override val serialNumber: String) : CardsScreenNewCardState(serialNumber)
}

data class CardAndTransactions(
    val card: CardInfo,
    val transactions: TransactionsState = TransactionsState.Loading,
)

fun List<CardAndTransactions>.cards(): List<CardInfo> = this.map { it.card }
fun List<CardInfo>.andTransactions(transactions: Map<CardId, TransactionsState> = emptyMap()): List<CardAndTransactions> =
    this.map { CardAndTransactions(it, transactions.getOrElse(it.serialNumber) { TransactionsState.Empty }) }
