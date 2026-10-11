package com.sloy.sevibus.data.api.model

import com.sloy.sevibus.domain.model.CardTransaction
import com.sloy.sevibus.domain.model.CardTransactionKind
import com.sloy.sevibus.domain.model.Line
import com.sloy.sevibus.domain.model.LineColor
import com.sloy.sevibus.domain.model.LineSummary
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import java.time.LocalDateTime

class CardTransactionDtoMappersTest {

    private val line02 = Line(label = "02", description = "", color = LineColor.Red, group = "", id = 2, routes = emptyList())
    private val lines = listOf(line02)
    private val date = "2026-10-09T20:16:00"

    @Test
    fun `maps a new backend trip`() {
        val tx = CardTransactionDto(900000001, date, "validation", amount = 46, lineLabel = "02", busId = 1111, people = 2,
            kind = "trip", amountCents = 46, balanceAfter = 452).toDomain(lines)

        expectThat(tx).isEqualTo(
            CardTransaction(900000001, LocalDateTime.of(2026, 10, 9, 20, 16), CardTransactionKind.TRIP,
                line = LineSummary(2, "02", LineColor.Red), amount = 46, balanceAfter = 452, people = 2)
        )
    }

    @Test
    fun `uses amountCents over the legacy amount so free trips have no amount`() {
        val tx = CardTransactionDto(900000001, date, "validation", amount = 0, lineLabel = "02", kind = "trip", amountCents = null).toDomain(lines)

        expectThat(tx?.amount).isNull()
    }

    @Test
    fun `maps a pending online purchase`() {
        val tx = CardTransactionDto(900000001, date, "online_purchase", amount = 1000, kind = "top_up", pending = true, amountCents = 1000).toDomain(lines)

        expectThat(tx).isNotNull().and {
            get { kind }.isEqualTo(CardTransactionKind.TOP_UP)
            get { pending }.isEqualTo(true)
            get { line }.isNull()
        }
    }

    @Test
    fun `falls back to the legacy operation for an old backend`() {
        expectThat(CardTransactionDto(900000001, date, "validation", amount = 46, lineLabel = "02", busId = 1).toDomain(lines)).isNotNull().and {
            get { kind }.isEqualTo(CardTransactionKind.TRIP)
            get { amount }.isEqualTo(46)
        }
        expectThat(CardTransactionDto(900000001, date, "topup", amount = 1000).toDomain(lines)?.kind).isEqualTo(CardTransactionKind.TOP_UP)
        expectThat(CardTransactionDto(900000001, date, "transfer", lineLabel = "02").toDomain(lines)?.kind).isEqualTo(CardTransactionKind.TRANSFER)
        expectThat(CardTransactionDto(900000001, date, "renewal").toDomain(lines)).isNull()
    }

    @Test
    fun `keeps a trip on an unknown line with an unknown colour`() {
        val tx = CardTransactionDto(900000001, date, "validation", lineLabel = "LN", kind = "trip").toDomain(lines)

        expectThat(tx?.line?.label).isEqualTo("LN")
        expectThat(tx?.line?.color).isEqualTo(LineColor.Unknown)
    }

    @Test
    fun `drops a row with an unparseable date instead of throwing`() {
        expectThat(CardTransactionDto(900000001, "09-10-2026T20:16:00", "validation", kind = "trip").toDomain(lines)).isNull()
    }
}
