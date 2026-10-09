package com.sloy.debugmenu.events

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.LocalDateTime
import java.time.ZoneOffset

class EventFormatTest {

    private val millis = LocalDateTime.of(2026, 10, 8, 16, 21, 8, 100_000_000).toInstant(ZoneOffset.UTC).toEpochMilli()

    @Test
    fun `clock time has seconds`() {
        expectThat(millis.toClockTime(ZoneOffset.UTC)).isEqualTo("16:21:08")
    }

    @Test
    fun `clock time with millis`() {
        expectThat(millis.toClockTimeMillis(ZoneOffset.UTC)).isEqualTo("16:21:08.100")
    }

    @Test
    fun `durations under 10 seconds have one decimal`() {
        expectThat(formatDuration(3_385)).isEqualTo("3.4s")
        expectThat(formatDuration(1_280)).isEqualTo("1.3s")
    }

    @Test
    fun `durations under a minute are whole seconds`() {
        expectThat(formatDuration(14_000)).isEqualTo("14s")
    }

    @Test
    fun `durations of a minute or more have minutes and seconds`() {
        expectThat(formatDuration(132_580)).isEqualTo("2m 13s")
        expectThat(formatDuration(94_000)).isEqualTo("1m 34s")
    }

    @Test
    fun `durations never show 60 seconds`() {
        expectThat(formatDuration(59_700)).isEqualTo("1m 0s")
        expectThat(formatDuration(119_600)).isEqualTo("2m 0s")
    }

    @Test
    fun `whole seconds`() {
        expectThat(formatSeconds(14)).isEqualTo("14s")
        expectThat(formatSeconds(97)).isEqualTo("1m 37s")
    }

    @Test
    fun `delta under a minute has two decimals`() {
        expectThat(formatDelta(650)).isEqualTo("+0.65s")
        expectThat(formatDelta(15)).isEqualTo("+0.02s")
    }

    @Test
    fun `delta of a minute or more uses minutes`() {
        expectThat(formatDelta(132_580)).isEqualTo("+2m 13s")
    }

    @Test
    fun `chronological sorts by timestamp and keeps insertion order for ties`() {
        val newestFirst = listOf(
            CapturedEvent("C", timestampMillis = 20, id = "c"),
            CapturedEvent("B", timestampMillis = 10, id = "b"),
            CapturedEvent("A", timestampMillis = 10, id = "a"),
        )
        expectThat(newestFirst.chronological().map { it.id }).isEqualTo(listOf("a", "b", "c"))
    }
}
