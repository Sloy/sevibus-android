package com.sloy.sevibus.ui.formatter

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object DateFormatter {

    private val dayMonthTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM · HH:mm")
    private val dayMonthFormatter = DateTimeFormatter.ofPattern("EEE d MMM")
    private val dayMonthYearFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

    fun dayMonthTime(dateTime: LocalDateTime): String {
        return dateTime.format(dayMonthTimeFormatter)
    }

    fun dayMonthYear(date: LocalDate): String {
        return date.format(dayMonthYearFormatter)
    }


}
