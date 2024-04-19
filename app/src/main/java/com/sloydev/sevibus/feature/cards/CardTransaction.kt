package com.sloydev.sevibus.feature.cards

import com.sloydev.sevibus.feature.lines.Line
import com.sloydev.sevibus.ui.formatter.MoneyFormatter
import java.time.LocalDateTime

sealed class CardTransaction(open val amountMillis: Int, open val date: LocalDateTime) {
    data class Validation(override val amountMillis: Int, override val date: LocalDateTime, val line: Line, val people: Int = 1) :
        CardTransaction(amountMillis, date)

    data class TopUp(override val amountMillis: Int, override val date: LocalDateTime) : CardTransaction(amountMillis, date)

    fun formattedAmount() = MoneyFormatter.fromMillis(amountMillis, showPlusWhenPositive = true)
}


