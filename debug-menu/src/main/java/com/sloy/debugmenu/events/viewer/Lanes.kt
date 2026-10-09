package com.sloy.debugmenu.events.viewer

import com.sloy.debugmenu.events.CapturedEvent
import com.sloy.debugmenu.events.EventType
import com.sloy.debugmenu.events.chronological
import com.sloy.debugmenu.events.formatDuration
import com.sloy.debugmenu.events.toClockTime
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import kotlin.math.abs

private const val MIN_SPAN_MILLIS = 5_000L
private const val PADDING_FRACTION = 0.05
private const val AXIS_LABELS = 5

internal enum class Lane { SCREENS, CLICKS, EVENTS }

internal data class LaneBar(val key: String, val start: Float, val end: Float, val label: String)

internal data class LaneMark(val key: String, val x: Float)

internal data class LanesModel(
    val range: String,
    val span: String,
    val screens: List<LaneBar>,
    val clicks: List<LaneMark>,
    val events: List<LaneMark>,
    val axis: List<Pair<Float, String>>,
)

internal fun lanesModel(events: List<CapturedEvent>, fromMillis: Long, toMillis: Long): LanesModel {
    val all = events.chronological()
    val span = maxOf(toMillis - fromMillis, MIN_SPAN_MILLIS)
    val spanStart = fromMillis - (span - (toMillis - fromMillis)) / 2
    val padding = (span * PADDING_FRACTION).toLong()
    val windowStart = spanStart - padding
    val windowEnd = spanStart + span + padding
    fun x(millis: Long): Float = ((millis - windowStart).toDouble() / (windowEnd - windowStart)).toFloat()
    fun inWindow(event: CapturedEvent) = event.timestampMillis in windowStart..windowEnd

    val screens = screenSpans(all).mapNotNull { span ->
        val end = span.endMillis ?: windowEnd
        if (end < windowStart || span.startMillis > windowEnd) return@mapNotNull null
        LaneBar(span.firstEventId, x(span.startMillis).coerceIn(0f, 1f), x(end).coerceIn(0f, 1f), span.screen)
    }
    val clicks = all.filter { it.type == EventType.CLICK && inWindow(it) }.map { LaneMark(it.id, x(it.timestampMillis)) }
    val others = all.filter { it.type == EventType.OTHER && inWindow(it) }.map { LaneMark(it.id, x(it.timestampMillis)) }
    val axis = (0 until AXIS_LABELS).map { step ->
        val fraction = step / (AXIS_LABELS - 1f)
        val millis = windowStart + ((windowEnd - windowStart) * fraction).toLong()
        fraction to String.format(Locale.US, "%02ds", Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).second)
    }
    return LanesModel(
        range = "${fromMillis.toClockTime()} → ${toMillis.toClockTime()}",
        span = formatDuration(toMillis - fromMillis),
        screens = screens,
        clicks = clicks,
        events = others,
        axis = axis,
    )
}

internal fun LanesModel.markAt(lane: Lane, x: Float, tolerance: Float): String? = when (lane) {
    Lane.SCREENS -> screens.firstOrNull { x in it.start..it.end }?.key
    Lane.CLICKS -> clicks.nearest(x, tolerance)
    Lane.EVENTS -> events.nearest(x, tolerance)
}

private fun List<LaneMark>.nearest(x: Float, tolerance: Float): String? =
    filter { abs(it.x - x) <= tolerance }.minByOrNull { abs(it.x - x) }?.key
