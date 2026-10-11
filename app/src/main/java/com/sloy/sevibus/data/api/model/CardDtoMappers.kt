package com.sloy.sevibus.data.api.model

import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.domain.model.CardStatus
import com.sloy.sevibus.domain.model.CardTransaction
import com.sloy.sevibus.domain.model.CardTransactionKind
import com.sloy.sevibus.domain.model.Line
import com.sloy.sevibus.domain.model.LineColor
import com.sloy.sevibus.domain.model.LineSummary
import com.sloy.sevibus.domain.model.NextPeriod
import com.sloy.sevibus.domain.model.toSummary
import com.sloy.sevibus.infrastructure.SevLogger
import java.time.LocalDate
import java.time.LocalDateTime

fun String?.toLocalDateOrNull(): LocalDate? = this?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

fun NextPeriodDto.toDomain(): NextPeriod = NextPeriod(from.toLocalDateOrNull(), to.toLocalDateOrNull(), pendingFirstUse)

fun cardStatusOf(kind: String?, balance: Int?, alertCode: Int?, validFrom: String?, validTo: String?, nextPeriod: NextPeriod?): CardStatus =
    when {
        kind == "expired_card" -> CardStatus.ExpiredCard
        kind != "pass" && balance != null -> CardStatus.Balance(balance, alertCode)
        else -> CardStatus.Pass(validFrom.toLocalDateOrNull(), validTo.toLocalDateOrNull(), nextPeriod)
    }

fun CardInfoDto.toDomain(): CardInfo = CardInfo(
    serialNumber = serialNumber,
    code = code,
    type = type,
    status = cardStatusOf(kind, balance, alertCode, validFrom, validTo, nextPeriod?.toDomain()),
    customName = customName,
    cardExpiry = cardExpiry.toLocalDateOrNull(),
    resultCode = resultCode,
)

private const val UNKNOWN_LINE_ID = -1

private fun kindOf(kind: String?, legacyOperation: String): CardTransactionKind? = when (kind) {
    "trip" -> CardTransactionKind.TRIP
    "transfer" -> CardTransactionKind.TRANSFER
    "top_up" -> CardTransactionKind.TOP_UP
    "pass_purchase" -> CardTransactionKind.PASS_PURCHASE
    "pass_renewal" -> CardTransactionKind.PASS_RENEWAL
    null -> when (legacyOperation) {
        "validation" -> CardTransactionKind.TRIP
        "transfer" -> CardTransactionKind.TRANSFER
        "topup" -> CardTransactionKind.TOP_UP
        else -> null
    }
    else -> null
}

fun CardTransactionDto.toDomain(lines: List<Line>): CardTransaction? {
    val transactionKind = kindOf(kind, operation) ?: run {
        SevLogger.logW(msg = "Unknown card transaction kind=$kind operation=$operation")
        return null
    }
    val parsedDate = runCatching { LocalDateTime.parse(date) }.getOrElse {
        SevLogger.logW(it, "Unparseable card transaction date $date")
        return null
    }
    val lineSummary = lineLabel?.let { label ->
        lines.find { it.label == label }?.toSummary() ?: LineSummary(UNKNOWN_LINE_ID, label, LineColor.Unknown)
    }
    return CardTransaction(
        serialNumber = serialNumber,
        date = parsedDate,
        kind = transactionKind,
        line = lineSummary,
        amount = if (kind != null) amountCents else amount?.takeIf { it != 0 },
        balanceAfter = balanceAfter,
        people = people ?: 1,
        pending = pending,
    )
}
