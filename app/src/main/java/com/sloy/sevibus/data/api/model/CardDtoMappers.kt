package com.sloy.sevibus.data.api.model

import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.domain.model.CardTransaction
import com.sloy.sevibus.domain.model.Fare
import com.sloy.sevibus.domain.model.Line
import com.sloy.sevibus.domain.model.LineColor
import com.sloy.sevibus.domain.model.LineSummary
import com.sloy.sevibus.domain.model.Money
import com.sloy.sevibus.domain.model.NextPeriod
import com.sloy.sevibus.domain.model.PassPeriod
import com.sloy.sevibus.domain.model.PassValidity
import com.sloy.sevibus.domain.model.toSummary
import com.sloy.sevibus.infrastructure.SevLogger
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

fun String?.toLocalDateOrNull(): LocalDate? = this?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

private fun periodOf(from: String?, to: String?): PassPeriod? {
    val start = from.toLocalDateOrNull() ?: return null
    val end = to.toLocalDateOrNull() ?: return null
    return PassPeriod(start, end)
}

private fun CardNextPeriodDto.toDomain(): NextPeriod? = when (type) {
    "scheduled" -> periodOf(from, to)?.let { NextPeriod.Scheduled(it) }
    "on_first_use" -> NextPeriod.OnFirstUse
    else -> null
}

fun CardPassDto.toDomain(): PassValidity? {
    val period = periodOf(from, to) ?: run {
        SevLogger.logW(msg = "Unparseable pass period $from - $to")
        return null
    }
    return PassValidity(period, nextPeriod?.toDomain())
}

fun CardInfoDto.toDomain(): CardInfo {
    val expiresOn = expiresOn.toLocalDateOrNull()
    return when {
        expired -> CardInfo.ExpiredCard(serialNumber, code, type, customName, expiresOn)
        kind != "pass" && balance != null -> CardInfo.BalanceCard(serialNumber, code, type, Money(balance), customName, expiresOn)
        else -> CardInfo.PassCard(serialNumber, code, type, pass?.toDomain(), customName, expiresOn)
    }
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
    val balance = balanceAfter?.let(::Money)
    val transaction = when (operation) {
        "validation" -> line?.let {
            CardTransaction.Trip(serialNumber, parsedDate, it, people ?: 1, balance?.let { after -> Fare(money ?: Money(0), after) })
        }
        "transfer" -> line?.let { CardTransaction.Transfer(serialNumber, parsedDate, it) }
        "topup" -> if (money != null && balance != null) CardTransaction.TopUp(serialNumber, parsedDate, money, balance) else null
        "pending_purchase" -> money?.let { CardTransaction.PendingPurchase(serialNumber, parsedDate, it) }
        "pass_purchase" -> money?.let { CardTransaction.PassPurchase(serialNumber, parsedDate, it) }
        "pass_renewal" -> CardTransaction.PassRenewal(serialNumber, parsedDate, money?.takeIf { it.isPositive })
        else -> null
    }
    if (transaction == null) SevLogger.logW(msg = "Unsupported card transaction $this")
    return transaction
}
