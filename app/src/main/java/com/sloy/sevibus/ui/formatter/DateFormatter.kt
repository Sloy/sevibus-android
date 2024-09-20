package com.sloy.sevibus.ui.formatter

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object DateFormatter {

    private val dayMonthTimeFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM, HH:mm")
    private val dayMonthFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM")
    private val dayMonthYearFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

    fun dayMonthTime(dateTime: LocalDateTime): String {
        return dateTime.format(dayMonthTimeFormatter)
    }

    fun dayMonthYear(date: LocalDate): String {
        return date.format(dayMonthYearFormatter)
    }


}