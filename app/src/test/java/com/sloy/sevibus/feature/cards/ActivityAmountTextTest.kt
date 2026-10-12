package com.sloy.sevibus.feature.cards

import com.sloy.sevibus.domain.model.CardTransactionKind
import com.sloy.sevibus.domain.model.CardTransactionKind.PASS_PURCHASE
import com.sloy.sevibus.domain.model.CardTransactionKind.PASS_RENEWAL
import com.sloy.sevibus.domain.model.CardTransactionKind.TOP_UP
import com.sloy.sevibus.domain.model.CardTransactionKind.TRANSFER
import com.sloy.sevibus.domain.model.CardTransactionKind.TRIP
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue
import strikt.assertions.isNull
import java.time.LocalDateTime

class ActivityAmountTextTest {
    private fun row(kind: CardTransactionKind, amount: Int?, pending: Boolean = false, count: Int = 1) =
        ActivityRow(kind, LocalDateTime.of(2026, 10, 9, 20, 16), null, count, amount, null, pending)

    @Test fun `balance trip is negative with a real minus`() { expectThat(amountText(row(TRIP, 46), true)).isEqualTo(AmountText("−0,46\u00A0€", muted = false)) }
    @Test fun `free trip on a balance card`() { expectThat(amountText(row(TRIP, null), true)).isEqualTo(AmountText(FREE, muted = false)) }
    @Test fun `pass trip has no amount`() { expectThat(amountText(row(TRIP, null), false)).isNull() }
    @Test fun `top-up is positive`() { expectThat(amountText(row(TOP_UP, 1000), true)).isEqualTo(AmountText("+10,00\u00A0€", muted = false)) }
    @Test fun `pending top-up is muted`() { expectThat(amountText(row(TOP_UP, 1000, pending = true), true)).isEqualTo(AmountText("+10,00\u00A0€", muted = true)) }
    @Test fun `pass purchase has no sign`() { expectThat(amountText(row(PASS_PURCHASE, 2120), false)).isEqualTo(AmountText("21,20\u00A0€", muted = false)) }
    @Test fun `renewal without amount shows nothing`() { expectThat(amountText(row(PASS_RENEWAL, null), false)).isNull() }
    @Test fun `group trip shows the total by default`() { expectThat(amountText(row(TRIP, 82, count = 2), true)).isEqualTo(AmountText("−0,82\u00A0€", muted = false)) }
    @Test fun `group trip shows the price per person`() { expectThat(amountText(row(TRIP, 82, count = 2), true, perPerson = true)).isEqualTo(AmountText("−0,41\u00A0€ × 2", muted = false)) }
    @Test fun `group trip on a balance card has a per person amount`() { expectThat(hasPerPersonAmount(row(TRIP, 82, count = 2), true)).isTrue() }
    @Test fun `single trip has no per person amount`() { expectThat(hasPerPersonAmount(row(TRIP, 41), true)).isFalse() }
    @Test fun `free group trip has no per person amount`() { expectThat(hasPerPersonAmount(row(TRIP, null, count = 2), true)).isFalse() }
    @Test fun `group transfer has no per person amount`() { expectThat(hasPerPersonAmount(row(TRANSFER, null, count = 2), true)).isFalse() }
    @Test fun `transfer amount slot is handled by the composable`() { expectThat(amountText(row(TRANSFER, null), true)).isNull() }
}
