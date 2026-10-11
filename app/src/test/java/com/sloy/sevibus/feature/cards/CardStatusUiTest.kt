package com.sloy.sevibus.feature.cards

import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.domain.model.CardStatus
import com.sloy.sevibus.domain.model.CardTransaction
import com.sloy.sevibus.domain.model.CardTransactionKind
import com.sloy.sevibus.domain.model.NextPeriod
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import java.time.LocalDate
import java.time.LocalDateTime

class CardStatusUiTest {

    private val today = LocalDate.of(2026, 10, 10)
    private fun card(status: CardStatus, cardExpiry: LocalDate? = null) = CardInfo(900000001, 150, "Tarjeta 30 días", status, cardExpiry = cardExpiry)
    private val trip = CardTransaction(900000001, LocalDateTime.of(2026, 10, 9, 8, 0), CardTransactionKind.TRIP, amount = 46)

    @Test
    fun `normal balance`() {
        expectThat(cardStatusUi(card(CardStatus.Balance(1074)), listOf(trip), today)).isEqualTo(CardStatusUi.Balance(1074, isEmpty = false, isLow = false, tripsLeft = 24))
    }

    @Test
    fun `low balance shows trips left`() {
        expectThat(cardStatusUi(card(CardStatus.Balance(10)), listOf(trip), today)).isEqualTo(CardStatusUi.Balance(10, isEmpty = false, isLow = true, tripsLeft = 1))
    }

    @Test
    fun `zero and negative balances are empty`() {
        expectThat(cardStatusUi(card(CardStatus.Balance(0)), listOf(trip), today)).isEqualTo(CardStatusUi.Balance(0, isEmpty = true, isLow = false, tripsLeft = null))
        expectThat(cardStatusUi(card(CardStatus.Balance(-44)), listOf(trip), today)).isEqualTo(CardStatusUi.Balance(-44, isEmpty = true, isLow = false, tripsLeft = null))
    }

    @Test
    fun `active pass`() {
        val status = cardStatusUi(card(CardStatus.Pass(LocalDate.of(2026, 10, 6), LocalDate.of(2026, 11, 4), null)), emptyList(), today)
        expectThat(status).isEqualTo(CardStatusUi.Pass(LocalDate.of(2026, 11, 4), hasNextPeriod = false, progress = PassProgress(LocalDate.of(2026, 10, 6), 5f / 30f, 25), expiredAgo = null))
    }

    @Test
    fun `pass on its last day`() {
        val status = cardStatusUi(card(CardStatus.Pass(LocalDate.of(2026, 9, 11), today, null)), emptyList(), today) as CardStatusUi.Pass
        expectThat(status.progress?.daysLeft).isEqualTo(0)
    }

    @Test
    fun `pass with next period paid`() {
        val status = cardStatusUi(card(CardStatus.Pass(LocalDate.of(2026, 9, 14), LocalDate.of(2026, 10, 13), NextPeriod(null, null, true))), emptyList(), today) as CardStatusUi.Pass
        expectThat(status.hasNextPeriod).isEqualTo(true)
        expectThat(status.progress?.daysLeft).isEqualTo(3)
    }

    @Test
    fun `expired pass`() {
        val status = cardStatusUi(card(CardStatus.Pass(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 8, 1), null)), emptyList(), today)
        expectThat(status).isEqualTo(CardStatusUi.Pass(LocalDate.of(2026, 8, 1), hasNextPeriod = false, progress = null, expiredAgo = Elapsed(2, ElapsedUnit.MONTHS)))
    }

    @Test
    fun `expired pass with a stale next period is just expired`() {
        val status = cardStatusUi(card(CardStatus.Pass(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 8, 1), NextPeriod(null, null, true))), emptyList(), today) as CardStatusUi.Pass
        expectThat(status.hasNextPeriod).isEqualTo(false)
    }

    @Test
    fun `pass not started yet has no bar`() {
        val status = cardStatusUi(card(CardStatus.Pass(LocalDate.of(2026, 10, 20), LocalDate.of(2026, 11, 18), null)), emptyList(), today) as CardStatusUi.Pass
        expectThat(status.progress).isNull()
        expectThat(status.expiredAgo).isNull()
    }

    @Test
    fun `pass without dates shows no status block`() {
        expectThat(cardStatusUi(card(CardStatus.Pass(null, null, null)), emptyList(), today)).isEqualTo(CardStatusUi.None)
    }

    @Test
    fun `expired card`() {
        expectThat(cardStatusUi(card(CardStatus.ExpiredCard, cardExpiry = LocalDate.of(2025, 9, 30)), emptyList(), today))
            .isEqualTo(CardStatusUi.ExpiredCard(LocalDate.of(2025, 9, 30)))
    }

    @Test
    fun `details rows`() {
        val pass = card(CardStatus.Pass(LocalDate.of(2026, 9, 14), LocalDate.of(2026, 10, 13), NextPeriod(null, null, true)), cardExpiry = LocalDate.of(2028, 5, 21))
        expectThat(cardDetailsUi(pass, today)).isEqualTo(CardDetailsUi(pass.formattedSerialNumber, PeriodRow(LocalDate.of(2026, 9, 14), LocalDate.of(2026, 10, 13), isCurrentPeriod = true), LocalDate.of(2028, 5, 21)))

        val expired = card(CardStatus.ExpiredCard, cardExpiry = LocalDate.of(2025, 9, 30))
        expectThat(cardDetailsUi(expired, today)).isEqualTo(CardDetailsUi(expired.formattedSerialNumber, null, null))
    }
}
