package com.sloy.sevibus.feature.cards

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.Period
import java.time.format.TextStyle
import java.util.Locale

enum class ElapsedUnit { DAYS, MONTHS, YEARS }

data class Elapsed(val amount: Int, val unit: ElapsedUnit)

object CardDateFormatter {

    private val spanish = Locale.forLanguageTag("es-ES")

    private fun shortMonth(date: LocalDate): String =
        date.month.getDisplayName(TextStyle.SHORT, spanish).removeSuffix(".").lowercase(spanish).take(3)

    private fun shortWeekday(date: LocalDate): String =
        date.dayOfWeek.getDisplayName(TextStyle.SHORT, spanish).removeSuffix(".").lowercase(spanish).take(3)

    fun dayMonth(date: LocalDate): String = "${date.dayOfMonth} ${shortMonth(date)}"

    fun date(date: LocalDate): String = "${dayMonth(date)} ${date.year}"

    fun range(from: LocalDate, to: LocalDate): String =
        if (from.year == to.year) "${dayMonth(from)} – ${date(to)}" else "${date(from)} – ${date(to)}"

    fun dateTime(dateTime: LocalDateTime): String {
        val day = dateTime.toLocalDate()
        return "${shortWeekday(day)} ${dayMonth(day)} · ${time(dateTime)}"
    }

    fun time(dateTime: LocalDateTime): String = "%02d:%02d".format(Locale.ROOT, dateTime.hour, dateTime.minute)

    fun month(year: Int, month: Int): String =
        Month.of(month).getDisplayName(TextStyle.FULL_STANDALONE, spanish).replaceFirstChar { it.titlecase(spanish) } + " $year"

    fun elapsed(from: LocalDate, to: LocalDate): Elapsed {
        val period = Period.between(from, to)
        return when {
            period.years > 0 -> Elapsed(period.years, ElapsedUnit.YEARS)
            period.months > 0 -> Elapsed(period.toTotalMonths().toInt(), ElapsedUnit.MONTHS)
            else -> Elapsed(period.days.coerceAtLeast(1), ElapsedUnit.DAYS)
        }
    }
}
