package com.sloy.sevibus.data.api.model

import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.domain.model.CardTransaction
import com.sloy.sevibus.domain.model.Fare
import com.sloy.sevibus.domain.model.Line
import com.sloy.sevibus.domain.model.LineColor
import com.sloy.sevibus.domain.model.LineSummary
import com.sloy.sevibus.domain.model.Money
import com.sloy.sevibus.domain.model.toSummary
import com.sloy.sevibus.infrastructure.SevLogger
import java.time.Duration
import java.time.LocalDateTime

fun CardInfoDto.fromDto(): CardInfo = when (balance) {
    null -> CardInfo.PassCard(serialNumber, code, type, customName)
    else -> CardInfo.BalanceCard(serialNumber, code, type, Money(balance), customName)
}

private const val UNKNOWN_LINE_ID = -1
private val TRANSFER_WINDOW: Duration = Duration.ofMinutes(60)

fun List<CardTransactionDto>.toDomain(lines: List<Line>): List<CardTransaction> {
    val oldestFirst = mutableListOf<CardTransaction>()
    for (transaction in asReversed().mapNotNull { it.toDomain(lines) }) {
        val previous = oldestFirst.lastOrNull()
        if (transaction is CardTransaction.Transfer && previous is CardTransaction.Trip && previous.continuesWith(transaction)) {
            oldestFirst[oldestFirst.lastIndex] = previous.copy(transfers = previous.transfers + transaction)
        } else {
            oldestFirst += transaction
        }
    }
    return oldestFirst.asReversed()
}

private fun CardTransaction.Trip.continuesWith(transfer: CardTransaction.Transfer): Boolean {
    val lastLeg = transfers.lastOrNull()?.date ?: date
    return transfer.date.toLocalDate() == date.toLocalDate() &&
        !transfer.date.isBefore(lastLeg) &&
        Duration.between(lastLeg, transfer.date) <= TRANSFER_WINDOW
}

private fun CardTransactionDto.lineOf(lines: List<Line>): LineSummary? = lineLabel?.let { label ->
    lines.find { it.label == label }?.toSummary() ?: LineSummary(UNKNOWN_LINE_ID, label, LineColor.Unknown)
}

private fun CardTransactionDto.toDomain(lines: List<Line>): CardTransaction? {
    val parsedDate = runCatching { LocalDateTime.parse(date) }.getOrElse {
        SevLogger.logW(it, "Unparseable card transaction date $date")
        return null
    }
    val line = lineOf(lines)
    val money = amount?.let(::Money)
    val transaction = when (operation) {
        "validation" -> line?.let { CardTransaction.Trip(serialNumber, parsedDate, it, people ?: 1, money?.let(::Fare)) }
        "transfer" -> line?.let { CardTransaction.Transfer(serialNumber, parsedDate, it) }
        "topup" -> money?.let { CardTransaction.TopUp(serialNumber, parsedDate, it) }
        else -> null
    }
    if (transaction == null) SevLogger.logW(msg = "Unsupported card transaction $this")
    return transaction
}
