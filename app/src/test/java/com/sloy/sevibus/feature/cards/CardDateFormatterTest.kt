package com.sloy.sevibus.feature.cards

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.LocalDate
import java.time.LocalDateTime

class CardDateFormatterTest {

    @Test
    fun `formats dates the Spanish way without dots`() {
        expectThat(CardDateFormatter.date(LocalDate.of(2026, 11, 4))).isEqualTo("4 nov 2026")
        expectThat(CardDateFormatter.date(LocalDate.of(2026, 9, 14))).isEqualTo("14 sep 2026")
        expectThat(CardDateFormatter.dayMonth(LocalDate.of(2026, 10, 6))).isEqualTo("6 oct")
    }

    @Test
    fun `repeats the year in a range only when it differs`() {
        expectThat(CardDateFormatter.range(LocalDate.of(2026, 10, 6), LocalDate.of(2026, 11, 4))).isEqualTo("6 oct – 4 nov 2026")
        expectThat(CardDateFormatter.range(LocalDate.of(2025, 12, 14), LocalDate.of(2026, 1, 13))).isEqualTo("14 dic 2025 – 13 ene 2026")
    }

    @Test
    fun `formats a transaction date and time`() {
        expectThat(CardDateFormatter.dateTime(LocalDateTime.of(2026, 10, 9, 20, 16))).isEqualTo("vie 9 oct · 20:16")
        expectThat(CardDateFormatter.dateTime(LocalDateTime.of(2026, 10, 5, 7, 5))).isEqualTo("lun 5 oct · 07:05")
    }

    @Test
    fun `capitalises month headers`() {
        expectThat(CardDateFormatter.month(2026, 10)).isEqualTo("Octubre 2026")
    }

    @Test
    fun `measures elapsed time in the largest whole unit`() {
        val end = LocalDate.of(2026, 8, 1)
        expectThat(CardDateFormatter.elapsed(end, LocalDate.of(2026, 8, 4))).isEqualTo(Elapsed(3, ElapsedUnit.DAYS))
        expectThat(CardDateFormatter.elapsed(end, LocalDate.of(2026, 10, 10))).isEqualTo(Elapsed(2, ElapsedUnit.MONTHS))
        expectThat(CardDateFormatter.elapsed(end, LocalDate.of(2027, 9, 1))).isEqualTo(Elapsed(1, ElapsedUnit.YEARS))
        expectThat(CardDateFormatter.elapsed(end, LocalDate.of(2026, 8, 2))).isEqualTo(Elapsed(1, ElapsedUnit.DAYS))
    }
}
