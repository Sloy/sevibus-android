package com.sloy.sevibus.feature.cards

import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.domain.model.CardTransaction
import com.sloy.sevibus.domain.model.Fare
import com.sloy.sevibus.domain.model.Money
import com.sloy.sevibus.domain.model.toSummary
import com.sloy.sevibus.domain.model.NextPeriod
import com.sloy.sevibus.domain.model.PassPeriod
import com.sloy.sevibus.domain.model.PassValidity
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import java.time.LocalDate
import java.time.LocalDateTime

class CardStatusUiTest {

    private val today = LocalDate.of(2026, 10, 10)
    private fun balanceCard(balance: Int) = CardInfo.BalanceCard(900000001, 30, "Bonobús con transbordo", Money(balance))
    private fun passCard(from: LocalDate, to: LocalDate, nextPeriod: NextPeriod? = null, expiresOn: LocalDate? = null) =
        CardInfo.PassCard(900000001, 150, "Tarjeta 30 días", PassValidity(PassPeriod(from, to), nextPeriod), expiresOn = expiresOn)
    private fun expiredCard(expiresOn: LocalDate) = CardInfo.ExpiredCard(900000001, 116, "Estudiante mensual", expiresOn = expiresOn)
    private val trip = CardTransaction.Trip(900000001, LocalDateTime.of(2026, 10, 9, 8, 0), Stubs.lines[0].toSummary(), 1, Fare(Money(46), Money(1074)))

    @Test
    fun `normal balance`() {
        expectThat(cardStatusUi(balanceCard(1074), listOf(trip), today)).isEqualTo(CardStatusUi.Balance(Money(1074), isEmpty = false, isLow = false, tripsLeft = 24))
    }

    @Test
    fun `low balance shows trips left`() {
        expectThat(cardStatusUi(balanceCard(10), listOf(trip), today)).isEqualTo(CardStatusUi.Balance(Money(10), isEmpty = false, isLow = true, tripsLeft = 1))
    }

    @Test
    fun `zero and negative balances are empty`() {
        expectThat(cardStatusUi(balanceCard(0), listOf(trip), today)).isEqualTo(CardStatusUi.Balance(Money(0), isEmpty = true, isLow = false, tripsLeft = null))
        expectThat(cardStatusUi(balanceCard(-44), listOf(trip), today)).isEqualTo(CardStatusUi.Balance(Money(-44), isEmpty = true, isLow = false, tripsLeft = null))
    }

    @Test
    fun `active pass`() {
        val status = cardStatusUi(passCard(LocalDate.of(2026, 10, 6), LocalDate.of(2026, 11, 4)), emptyList(), today)
        expectThat(status).isEqualTo(CardStatusUi.Pass(LocalDate.of(2026, 11, 4), hasNextPeriod = false, progress = PassProgress(LocalDate.of(2026, 10, 6), 5f / 30f, 25), expiredAgo = null))
    }

    @Test
    fun `pass on its last day`() {
        val status = cardStatusUi(passCard(LocalDate.of(2026, 9, 11), today), emptyList(), today) as CardStatusUi.Pass
        expectThat(status.progress?.daysLeft).isEqualTo(0)
    }

    @Test
    fun `pass with next period paid`() {
        val status = cardStatusUi(passCard(LocalDate.of(2026, 9, 14), LocalDate.of(2026, 10, 13), NextPeriod.OnFirstUse), emptyList(), today) as CardStatusUi.Pass
        expectThat(status.hasNextPeriod).isEqualTo(true)
        expectThat(status.progress?.daysLeft).isEqualTo(3)
    }

    @Test
    fun `expired pass`() {
        val status = cardStatusUi(passCard(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 8, 1)), emptyList(), today)
        expectThat(status).isEqualTo(CardStatusUi.Pass(LocalDate.of(2026, 8, 1), hasNextPeriod = false, progress = null, expiredAgo = Elapsed(2, ElapsedUnit.MONTHS)))
    }

    @Test
    fun `expired pass with a stale next period is just expired`() {
        val status = cardStatusUi(passCard(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 8, 1), NextPeriod.OnFirstUse), emptyList(), today) as CardStatusUi.Pass
        expectThat(status.hasNextPeriod).isEqualTo(false)
    }

    @Test
    fun `pass not started yet has no bar`() {
        val status = cardStatusUi(passCard(LocalDate.of(2026, 10, 20), LocalDate.of(2026, 11, 18)), emptyList(), today) as CardStatusUi.Pass
        expectThat(status.progress).isNull()
        expectThat(status.expiredAgo).isNull()
    }

    @Test
    fun `pass not refreshed yet shows no status block`() {
        expectThat(cardStatusUi(CardInfo.PassCard(900000001, 150, "Tarjeta 30 días", validity = null), emptyList(), today)).isEqualTo(CardStatusUi.None)
    }

    @Test
    fun `expired card`() {
        expectThat(cardStatusUi(expiredCard(LocalDate.of(2025, 9, 30)), emptyList(), today))
            .isEqualTo(CardStatusUi.ExpiredCard(LocalDate.of(2025, 9, 30)))
    }

    @Test
    fun `details rows`() {
        val pass = passCard(LocalDate.of(2026, 9, 14), LocalDate.of(2026, 10, 13), NextPeriod.OnFirstUse, expiresOn = LocalDate.of(2028, 5, 21))
        expectThat(cardDetailsUi(pass, today)).isEqualTo(CardDetailsUi(pass.formattedSerialNumber, PeriodRow(LocalDate.of(2026, 9, 14), LocalDate.of(2026, 10, 13), isCurrentPeriod = true), LocalDate.of(2028, 5, 21)))

        val expired = expiredCard(LocalDate.of(2025, 9, 30))
        expectThat(cardDetailsUi(expired, today)).isEqualTo(CardDetailsUi(expired.formattedSerialNumber, null, null))
    }
}
