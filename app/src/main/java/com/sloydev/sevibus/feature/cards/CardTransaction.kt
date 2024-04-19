package com.sloydev.sevibus.feature.cards

import com.sloydev.sevibus.feature.lines.Line
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

sealed class CardTransaction(open val amountMillis: Int, open val date: LocalDateTime) {
    data class Validation(override val amountMillis: Int, override val date: LocalDateTime, val line: Line, val people: Int = 1) :
        CardTransaction(amountMillis, date)

    data class TopUp(override val amountMillis: Int, override val date: LocalDateTime) : CardTransaction(amountMillis, date)

}

fun CardTransaction.formattedAmount(): String {
    val sign = when (this) {
        is CardTransaction.TopUp -> "+"
        is CardTransaction.Validation -> "-"
    }
    return "$sign${amountMillis / 1000f} €"
}

