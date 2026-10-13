package com.sloy.debugmenu.events

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val CLOCK: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
private val CLOCK_MILLIS: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")

internal fun Long.toClockTime(zone: ZoneId = ZoneId.systemDefault()): String = Instant.ofEpochMilli(this).atZone(zone).format(CLOCK)

internal fun Long.toClockTimeMillis(zone: ZoneId = ZoneId.systemDefault()): String =
    Instant.ofEpochMilli(this).atZone(zone).format(CLOCK_MILLIS)

internal fun formatDuration(millis: Long): String {
    val tenths = (millis + 50) / 100
    if (tenths < 100) return String.format(Locale.US, "%.1fs", tenths / 10.0)
    return formatSeconds((millis + 500) / 1000)
}

internal fun formatSeconds(seconds: Long): String =
    if (seconds < 60) "${seconds}s" else "${seconds / 60}m ${seconds % 60}s"

internal fun formatDelta(millis: Long): String =
    if (millis < 59_995) "+${BigDecimal.valueOf(millis, 3).setScale(2, RoundingMode.HALF_UP).toPlainString()}s"
    else "+${formatDuration(millis)}"
