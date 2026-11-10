package com.sloy.sevibus.feature.cards

import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.CardId
import com.sloy.sevibus.domain.model.CardInfo
import java.time.LocalDate

internal object CardsScreenPreviewData {

    private val today: LocalDate = Stubs.cardCasesToday

    fun balanceCardUi(card: CardInfo = Stubs.balanceCard, transactions: TransactionsState = TransactionsState.Empty): CardUi =
        cardUi(card, transactions)

    fun passActive(): CardUi = fromCase("PassActive")

    fun passLastDay(): CardUi = fromCase("PassActive", today = LocalDate.of(2026, 11, 4))

    fun passNextPeriod(): CardUi = fromCase("PassNextPeriod")

    fun passExpired(): CardUi = fromCase("PassExpired")

    fun cardExpired(): CardUi = fromCase("CardExpired")

    fun content(card: CardUi): CardsScreenState.Content = CardsScreenState.Content(listOf(card))

    fun cardUi(card: CardInfo, transactions: TransactionsState, today: LocalDate = this.today): CardUi {
        val loadedTransactions = (transactions as? TransactionsState.Loaded)?.transactions.orEmpty()
        return CardUi(card, transactions, cardStatusUi(card, loadedTransactions, today))
    }

    private fun fromCase(name: String, today: LocalDate = this.today): CardUi {
        val case = Stubs.cardCases.first { it.name == name }
        return cardUi(case.card, TransactionsState.Loaded(case.transactions), today)
    }
}

internal fun List<CardInfo>.andTransactions(transactions: Map<CardId, TransactionsState> = emptyMap()): List<CardUi> =
    map { CardsScreenPreviewData.cardUi(it, transactions.getOrElse(it.serialNumber) { TransactionsState.Empty }) }
