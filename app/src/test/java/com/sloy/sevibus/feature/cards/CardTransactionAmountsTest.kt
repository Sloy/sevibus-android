package com.sloy.sevibus.feature.cards

import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.CardTransaction
import com.sloy.sevibus.domain.model.Fare
import com.sloy.sevibus.domain.model.Money
import com.sloy.sevibus.domain.model.toSummary
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNull
import strikt.assertions.isTrue
import java.time.LocalDateTime

class CardTransactionAmountsTest {

    private val date = LocalDateTime.of(2026, 10, 9, 20, 16)
    private val line = Stubs.lines[0].toSummary()

    private fun trip(fare: Fare?, people: Int = 1) = CardTransaction.Trip(1, date, line, people, fare)

    @Test fun `fare is negative with a real minus`() { expectThat(fareText(Fare(Money(46), Money(452)), 1, perPerson = false)).isEqualTo("−0,46 €") }
    @Test fun `free trip has no fare text`() { expectThat(fareText(Fare(Money(0), Money(452)), 1, perPerson = false)).isNull() }
    @Test fun `group trip shows the total by default`() { expectThat(fareText(Fare(Money(82), Money(452)), 2, perPerson = false)).isEqualTo("−0,82 €") }
    @Test fun `group trip shows the price per person`() { expectThat(fareText(Fare(Money(82), Money(452)), 2, perPerson = true)).isEqualTo("−0,41 € × 2") }
    @Test fun `paid group trip has a per person fare`() { expectThat(trip(Fare(Money(82), Money(452)), people = 2).hasPerPersonFare()).isTrue() }
    @Test fun `single trip has no per person fare`() { expectThat(trip(Fare(Money(41), Money(452))).hasPerPersonFare()).isFalse() }
    @Test fun `free group trip has no per person fare`() { expectThat(trip(Fare(Money(0), Money(452)), people = 2).hasPerPersonFare()).isFalse() }
    @Test fun `group trip on a pass has no per person fare`() { expectThat(trip(fare = null, people = 2).hasPerPersonFare()).isFalse() }

    @Test fun `top-up is positive`() {
        expectThat(purchaseAmountText(CardTransaction.TopUp(1, date, Money(1000), Money(1074)), true)).isEqualTo(AmountText("+10,00 €", muted = false))
    }

    @Test fun `pending purchase is muted, with a sign only on balance cards`() {
        val pending = CardTransaction.PendingPurchase(1, date, Money(1000))
        expectThat(purchaseAmountText(pending, true)).isEqualTo(AmountText("+10,00 €", muted = true))
        expectThat(purchaseAmountText(pending, false)).isEqualTo(AmountText("10,00 €", muted = true))
    }

    @Test fun `pass purchase has no sign`() {
        expectThat(purchaseAmountText(CardTransaction.PassPurchase(1, date, Money(2120)), false)).isEqualTo(AmountText("21,20 €", muted = false))
    }

    @Test fun `renewal without amount shows nothing`() {
        expectThat(purchaseAmountText(CardTransaction.PassRenewal(1, date, amount = null), false)).isNull()
    }

    @Test fun `negative balance after uses a real minus`() { expectThat(signedMoney(Money(-44))).isEqualTo("−0,44 €") }
}
