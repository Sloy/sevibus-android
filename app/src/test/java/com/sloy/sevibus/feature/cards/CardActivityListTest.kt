package com.sloy.sevibus.feature.cards

import com.sloy.sevibus.domain.model.CardTransaction
import com.sloy.sevibus.domain.model.CardTransactionKind.TOP_UP
import com.sloy.sevibus.domain.model.CardTransactionKind.TRANSFER
import com.sloy.sevibus.domain.model.CardTransactionKind.TRIP
import com.sloy.sevibus.domain.model.LineColor
import com.sloy.sevibus.domain.model.LineSummary
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import java.time.LocalDateTime

class CardActivityListTest {

    private val l02 = LineSummary(2, "02", LineColor.Red)
    private val l32 = LineSummary(32, "32", LineColor.Blue)
    private fun at(day: Int, h: Int, m: Int, s: Int = 0, month: Int = 10) = LocalDateTime.of(2026, month, day, h, m, s)
    private fun tx(kind: com.sloy.sevibus.domain.model.CardTransactionKind, date: LocalDateTime, line: LineSummary? = l02, amount: Int? = 46, balance: Int? = null, people: Int = 1) =
        CardTransaction(900000001, date, kind, line, amount, balance, people)

    @Test
    fun `merges a group trip into one row`() {
        val rows = cardActivity(listOf(
            tx(TRIP, at(9, 20, 16, 30), amount = 46, balance = 360, people = 2),
            tx(TRIP, at(9, 20, 16, 5), amount = 46, balance = 406),
        )).single().rows

        expectThat(rows).hasSize(1)
        expectThat(rows[0]).isEqualTo(ActivityRow(TRIP, at(9, 20, 16, 5), l02, count = 2, amount = 92, balanceAfter = 360, pending = false))
    }

    @Test
    fun `merges three passengers`() {
        val rows = cardActivity(listOf(
            tx(TRIP, at(9, 20, 17), people = 3), tx(TRIP, at(9, 20, 16, 40), people = 2), tx(TRIP, at(9, 20, 16, 10)),
        )).single().rows
        expectThat(rows.single().count).isEqualTo(3)
    }

    @Test
    fun `merges group transfers with no amount`() {
        val rows = cardActivity(listOf(tx(TRANSFER, at(9, 21, 0, 30), amount = null, people = 2), tx(TRANSFER, at(9, 21, 0, 0), amount = null))).single().rows
        expectThat(rows.single()).isEqualTo(ActivityRow(TRANSFER, at(9, 21, 0, 0), l02, 2, null, null, false))
    }

    @Test
    fun `does not merge different lines, kinds, far apart or across midnight`() {
        val rows = cardActivity(listOf(
            tx(TRIP, at(10, 0, 0, 30)),
            tx(TRIP, at(9, 23, 59, 50)),
            tx(TRIP, at(9, 20, 10)),
            tx(TRIP, at(9, 20, 5)),
            tx(TRIP, at(9, 20, 4, 30), line = l32),
            tx(TRANSFER, at(9, 20, 4), amount = null),
        )).single().rows
        expectThat(rows).hasSize(6)
    }

    @Test
    fun `never merges top-ups`() {
        expectThat(cardActivity(listOf(tx(TOP_UP, at(9, 8, 0, 30), line = null, amount = 1000), tx(TOP_UP, at(9, 8, 0), line = null, amount = 1000))).single().rows).hasSize(2)
    }

    @Test
    fun `groups by month newest first`() {
        val months = cardActivity(listOf(tx(TRIP, at(2, 8, 0)), tx(TRIP, at(28, 8, 0, month = 9)), tx(TRIP, at(3, 8, 0, month = 8))))
        expectThat(months.map { it.month }).isEqualTo(listOf(10, 9, 8))
    }
}
