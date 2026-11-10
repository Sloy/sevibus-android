package com.sloy.sevibus.feature.cards

import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.CardId
import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.domain.model.CardTransaction
import java.time.LocalDate

internal object CardsScreenPreviewData {

    private val today: LocalDate = Stubs.cardCasesToday

    fun balanceCardUi(card: CardInfo = Stubs.balanceCard, transactions: TransactionsState = TransactionsState.Empty): CardUi =
        cardUi(card, transactions)

    fun balanceNormal(): CardUi = fromCase("BalanceNormal")

    fun balanceGroup(): CardUi = fromCase("BalanceGroup")

    fun balanceLow(): CardUi = fromCase("BalanceLow")

    fun balanceNegative(): CardUi = fromCase("BalanceNegative")

    fun passActive(): CardUi = fromCase("PassActive")

    fun passLastDay(): CardUi = fromCase("PassActive", today = LocalDate.of(2026, 11, 4))

    fun passNextPeriod(): CardUi = fromCase("PassNextPeriod")

    fun passExpired(): CardUi = fromCase("PassExpired")

    fun cardExpired(): CardUi = fromCase("CardExpired")

    fun passTransfers(): CardUi = fromCase("PassTransfers")

    fun content(card: CardUi): CardsScreenState.Content = CardsScreenState.Content(listOf(card))

    fun loadedTransactions(card: CardUi): List<CardTransaction> = (card.transactions as TransactionsState.Loaded).transactions

    fun cardUi(card: CardInfo, transactions: TransactionsState, today: LocalDate = this.today): CardUi {
        val loadedTransactions = (transactions as? TransactionsState.Loaded)?.transactions.orEmpty()
        return CardUi(card, transactions, cardStatusUi(card, loadedTransactions, today), cardDetailsUi(card, today))
    }

    private fun fromCase(name: String, today: LocalDate = this.today): CardUi {
        val case = Stubs.cardCases.first { it.name == name }
        return cardUi(case.card, TransactionsState.Loaded(case.transactions), today)
    }
}

internal fun List<CardInfo>.andTransactions(transactions: Map<CardId, TransactionsState> = emptyMap()): List<CardUi> =
    map { CardsScreenPreviewData.cardUi(it, transactions.getOrElse(it.serialNumber) { TransactionsState.Empty }) }
