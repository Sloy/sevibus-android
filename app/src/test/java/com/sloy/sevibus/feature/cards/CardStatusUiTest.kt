package com.sloy.sevibus.feature.cards

import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.domain.model.CardTransaction
import com.sloy.sevibus.domain.model.Fare
import com.sloy.sevibus.domain.model.Money
import com.sloy.sevibus.domain.model.NextPeriod
import com.sloy.sevibus.domain.model.PassPeriod
import com.sloy.sevibus.domain.model.PassValidity
import com.sloy.sevibus.domain.model.toSummary
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
        val card = balanceCard(1074)
        val transactions = listOf(trip)

        val status = cardStatusUi(card, transactions, today)

        expectThat(status).isEqualTo(CardStatusUi.Balance(Money(1074), isEmpty = false, isLow = false, tripsLeft = 24))
    }

    @Test
    fun `low balance shows trips left`() {
        val card = balanceCard(10)
        val transactions = listOf(trip)

        val status = cardStatusUi(card, transactions, today)

        expectThat(status).isEqualTo(CardStatusUi.Balance(Money(10), isEmpty = false, isLow = true, tripsLeft = 1))
    }

    @Test
    fun `zero balance is empty`() {
        val card = balanceCard(0)
        val transactions = listOf(trip)

        val status = cardStatusUi(card, transactions, today)

        expectThat(status).isEqualTo(CardStatusUi.Balance(Money(0), isEmpty = true, isLow = false, tripsLeft = null))
    }

    @Test
    fun `negative balance is empty`() {
        val card = balanceCard(-44)
        val transactions = listOf(trip)

        val status = cardStatusUi(card, transactions, today)

        expectThat(status).isEqualTo(CardStatusUi.Balance(Money(-44), isEmpty = true, isLow = false, tripsLeft = null))
    }

    @Test
    fun `active pass`() {
        val card = passCard(from = LocalDate.of(2026, 10, 6), to = LocalDate.of(2026, 11, 4))

        val status = cardStatusUi(card, emptyList(), today)

        val expectedProgress = CardStatusUi.Pass.Progress(validFrom = LocalDate.of(2026, 10, 6), fraction = 5f / 30f, daysLeft = 25)
        expectThat(status).isEqualTo(
            CardStatusUi.Pass(validTo = LocalDate.of(2026, 11, 4), hasNextPeriod = false, progress = expectedProgress, expiredAgo = null)
        )
    }

    @Test
    fun `pass on its last day`() {
        val card = passCard(from = LocalDate.of(2026, 9, 11), to = today)

        val status = cardStatusUi(card, emptyList(), today) as CardStatusUi.Pass

        expectThat(status.progress?.daysLeft).isEqualTo(0)
    }

    @Test
    fun `pass with next period paid`() {
        val card = passCard(from = LocalDate.of(2026, 9, 14), to = LocalDate.of(2026, 10, 13), nextPeriod = NextPeriod.OnFirstUse)

        val status = cardStatusUi(card, emptyList(), today) as CardStatusUi.Pass

        expectThat(status.hasNextPeriod).isEqualTo(true)
        expectThat(status.progress?.daysLeft).isEqualTo(3)
    }

    @Test
    fun `expired pass`() {
        val card = passCard(from = LocalDate.of(2026, 2, 1), to = LocalDate.of(2026, 8, 1))

        val status = cardStatusUi(card, emptyList(), today)

        expectThat(status).isEqualTo(
            CardStatusUi.Pass(validTo = LocalDate.of(2026, 8, 1), hasNextPeriod = false, progress = null, expiredAgo = Elapsed(2, ElapsedUnit.MONTHS))
        )
    }

    @Test
    fun `expired pass with a stale next period is just expired`() {
        val card = passCard(from = LocalDate.of(2026, 2, 1), to = LocalDate.of(2026, 8, 1), nextPeriod = NextPeriod.OnFirstUse)

        val status = cardStatusUi(card, emptyList(), today) as CardStatusUi.Pass

        expectThat(status.hasNextPeriod).isEqualTo(false)
    }

    @Test
    fun `pass not started yet has no bar`() {
        val card = passCard(from = LocalDate.of(2026, 10, 20), to = LocalDate.of(2026, 11, 18))

        val status = cardStatusUi(card, emptyList(), today) as CardStatusUi.Pass

        expectThat(status.progress).isNull()
        expectThat(status.expiredAgo).isNull()
    }

    @Test
    fun `pass not refreshed yet shows no status block`() {
        val card = CardInfo.PassCard(900000001, 150, "Tarjeta 30 días", validity = null)

        val status = cardStatusUi(card, emptyList(), today)

        expectThat(status).isEqualTo(CardStatusUi.None)
    }

    @Test
    fun `expired card`() {
        val card = expiredCard(expiresOn = LocalDate.of(2025, 9, 30))

        val status = cardStatusUi(card, emptyList(), today)

        expectThat(status).isEqualTo(CardStatusUi.ExpiredCard(expiredOn = LocalDate.of(2025, 9, 30)))
    }

    @Test
    fun `details of a pass show its period and physical expiry`() {
        val pass = passCard(
            from = LocalDate.of(2026, 9, 14),
            to = LocalDate.of(2026, 10, 13),
            nextPeriod = NextPeriod.OnFirstUse,
            expiresOn = LocalDate.of(2028, 5, 21),
        )

        val details = cardDetailsUi(pass, today)

        val expectedPeriod = CardDetailsUi.Period(from = LocalDate.of(2026, 9, 14), to = LocalDate.of(2026, 10, 13), isCurrentPeriod = true)
        expectThat(details).isEqualTo(CardDetailsUi(pass.formattedSerialNumber, expectedPeriod, physicalExpiry = LocalDate.of(2028, 5, 21)))
    }

    @Test
    fun `details of an expired card have no period nor physical expiry`() {
        val expired = expiredCard(expiresOn = LocalDate.of(2025, 9, 30))

        val details = cardDetailsUi(expired, today)

        expectThat(details).isEqualTo(CardDetailsUi(expired.formattedSerialNumber, period = null, physicalExpiry = null))
    }
}
