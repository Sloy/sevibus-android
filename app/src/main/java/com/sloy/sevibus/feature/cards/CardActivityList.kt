package com.sloy.sevibus.feature.cards

import com.sloy.sevibus.domain.model.CardTransaction
import com.sloy.sevibus.domain.model.CardTransactionKind
import com.sloy.sevibus.domain.model.LineSummary
import java.time.Duration
import java.time.LocalDateTime

data class ActivityRow(
    val kind: CardTransactionKind,
    val date: LocalDateTime,
    val line: LineSummary?,
    val count: Int,
    val amount: Int?,
    val balanceAfter: Int?,
    val pending: Boolean,
)

data class ActivityMonth(val year: Int, val month: Int, val rows: List<ActivityRow>)

private val GROUP_TRIP_WINDOW: Duration = Duration.ofSeconds(120)
private val MERGEABLE_KINDS = setOf(CardTransactionKind.TRIP, CardTransactionKind.TRANSFER)

fun cardActivity(transactions: List<CardTransaction>): List<ActivityMonth> =
    mergeGroupTrips(transactions)
        .groupBy { it.date.year to it.date.monthValue }
        .map { (yearMonth, rows) -> ActivityMonth(yearMonth.first, yearMonth.second, rows) }

private fun CardTransaction.toRow() = ActivityRow(kind, date, line, count = 1, amount, balanceAfter, pending)

private fun ActivityRow.absorbs(next: CardTransaction): Boolean =
    kind in MERGEABLE_KINDS && next.kind == kind && next.line?.label == line?.label &&
        next.date.toLocalDate() == date.toLocalDate() &&
        Duration.between(next.date, date).abs() <= GROUP_TRIP_WINDOW

private fun sumAmounts(a: Int?, b: Int?): Int? = if (a == null && b == null) null else (a ?: 0) + (b ?: 0)

private fun mergeGroupTrips(transactions: List<CardTransaction>): List<ActivityRow> {
    val rows = mutableListOf<ActivityRow>()
    var tapsInLastRow = 0
    var peopleInLastRow = 1
    for (transaction in transactions) {
        val last = rows.lastOrNull()
        if (last != null && last.absorbs(transaction)) {
            tapsInLastRow += 1
            peopleInLastRow = maxOf(peopleInLastRow, transaction.people)
            rows[rows.lastIndex] = last.copy(
                count = maxOf(tapsInLastRow, peopleInLastRow),
                amount = sumAmounts(last.amount, transaction.amount),
                date = transaction.date,
            )
        } else {
            tapsInLastRow = 1
            peopleInLastRow = transaction.people
            rows += transaction.toRow().copy(count = maxOf(1, transaction.people))
        }
    }
    return rows
}
