package com.sloy.sevibus.data.api.model

import com.sloy.sevibus.domain.model.CardTransaction
import com.sloy.sevibus.domain.model.Fare
import com.sloy.sevibus.domain.model.Line
import com.sloy.sevibus.domain.model.LineColor
import com.sloy.sevibus.domain.model.LineSummary
import com.sloy.sevibus.domain.model.Money
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import java.time.LocalDateTime

class CardTransactionDtoMappersTest {

    private val line02 = Line(label = "02", description = "", color = LineColor.Red, group = "", id = 2, routes = emptyList())
    private val lines = listOf(line02)
    private val summary02 = LineSummary(2, "02", LineColor.Red)
    private val serial = 900000001L

    private fun dto(date: String, operation: String, amount: Int? = null, lineLabel: String? = null, people: Int? = null, balanceAfter: Int? = null) =
        CardTransactionDto(serial, date, operation, amount, lineLabel, busId = 1, people = people, balanceAfter = balanceAfter)

    private fun at(day: Int, hour: Int, minute: Int) = LocalDateTime.of(2026, 10, day, hour, minute)

    @Test
    fun `maps a balance card trip with its fare`() {
        val transactions = listOf(dto("2026-10-09T20:16:00", "validation", amount = 92, lineLabel = "02", people = 2, balanceAfter = 452)).toDomain(lines)

        expectThat(transactions).containsExactly(CardTransaction.Trip(serial, at(9, 20, 16), summary02, 2, Fare(Money(92), Money(452))))
    }

    @Test
    fun `maps a free trip on a balance card and a trip on a pass`() {
        val transactions = listOf(
            dto("2026-10-09T20:16:00", "validation", amount = 0, lineLabel = "02", balanceAfter = 452),
            dto("2026-10-08T20:16:00", "validation", amount = 0, lineLabel = "02"),
        ).toDomain(lines)

        expectThat(transactions).containsExactly(
            CardTransaction.Trip(serial, at(9, 20, 16), summary02, 1, Fare(Money(0), Money(452))),
            CardTransaction.Trip(serial, at(8, 20, 16), summary02, 1, fare = null),
        )
    }

    @Test
    fun `maps purchases`() {
        val transactions = listOf(
            dto("2026-10-09T21:00:00", "pending_purchase", amount = 1000),
            dto("2026-10-09T20:00:00", "topup", amount = 1000, balanceAfter = 1074),
            dto("2026-10-08T20:00:00", "pass_purchase", amount = 2120),
            dto("2026-10-07T20:00:00", "pass_renewal", amount = 1690),
            dto("2026-10-06T20:00:00", "pass_renewal", amount = 0),
        ).toDomain(lines)

        expectThat(transactions).containsExactly(
            CardTransaction.PendingPurchase(serial, at(9, 21, 0), Money(1000)),
            CardTransaction.TopUp(serial, at(9, 20, 0), Money(1000), Money(1074)),
            CardTransaction.PassPurchase(serial, at(8, 20, 0), Money(2120)),
            CardTransaction.PassRenewal(serial, at(7, 20, 0), Money(1690)),
            CardTransaction.PassRenewal(serial, at(6, 20, 0), amount = null),
        )
    }

    @Test
    fun `falls back to an unknown line badge`() {
        val transactions = listOf(dto("2026-10-09T20:16:00", "validation", amount = 0, lineLabel = "N9")).toDomain(lines)

        expectThat(transactions).containsExactly(CardTransaction.Trip(serial, at(9, 20, 16), LineSummary(-1, "N9", LineColor.Unknown), 1, null))
    }

    @Test
    fun `drops unknown operations and unparseable dates`() {
        val transactions = listOf(
            dto("2026-10-09T20:16:00", "something_new", amount = 100),
            dto("9/10/2026", "validation", amount = 0, lineLabel = "02"),
        ).toDomain(lines)

        expectThat(transactions).isEmpty()
    }

    @Test
    fun `attaches transfers to the trip they continue`() {
        val transactions = listOf(
            dto("2026-10-05T13:45:39", "transfer", lineLabel = "28"),
            dto("2026-10-05T13:18:19", "transfer", lineLabel = "B4"),
            dto("2026-10-05T12:56:17", "validation", amount = 0, lineLabel = "02"),
        ).toDomain(lines)

        expectThat(transactions).containsExactly(
            CardTransaction.Trip(
                serial, at(5, 12, 56).plusSeconds(17), summary02, 1, null,
                transfers = listOf(
                    CardTransaction.Transfer(serial, at(5, 13, 18).plusSeconds(19), LineSummary(-1, "B4", LineColor.Unknown)),
                    CardTransaction.Transfer(serial, at(5, 13, 45).plusSeconds(39), LineSummary(-1, "28", LineColor.Unknown)),
                ),
            )
        )
    }

    @Test
    fun `measures the transfer window from the previous leg`() {
        val transactions = listOf(
            dto("2026-10-08T12:29:54", "transfer", lineLabel = "02"),
            dto("2026-10-08T11:52:37", "transfer", lineLabel = "02"),
            dto("2026-10-08T11:00:02", "validation", amount = 0, lineLabel = "02"),
        ).toDomain(lines)

        expectThat(transactions.map { (it as CardTransaction.Trip).transfers.size }).containsExactly(2)
    }

    @Test
    fun `keeps a transfer on its own when its trip isn't in the history or is too far`() {
        val transactions = listOf(
            dto("2026-10-08T14:31:27", "transfer", lineLabel = "02"),
            dto("2026-10-08T08:15:57", "transfer", lineLabel = "02"),
            dto("2026-10-07T20:00:00", "topup", amount = 1000, balanceAfter = 1074),
            dto("2026-10-07T19:50:00", "validation", amount = 46, lineLabel = "02", balanceAfter = 74),
        ).toDomain(lines)

        expectThat(transactions).containsExactly(
            CardTransaction.Transfer(serial, at(8, 14, 31).plusSeconds(27), summary02),
            CardTransaction.Transfer(serial, at(8, 8, 15).plusSeconds(57), summary02),
            CardTransaction.TopUp(serial, at(7, 20, 0), Money(1000), Money(1074)),
            CardTransaction.Trip(serial, at(7, 19, 50), summary02, 1, Fare(Money(46), Money(74))),
        )
    }
}
