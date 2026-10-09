package com.sloy.debugmenu.events.viewer

import com.sloy.debugmenu.events.CapturedEvent
import com.sloy.debugmenu.events.EventType
import com.sloy.debugmenu.events.chronological
import com.sloy.debugmenu.events.formatDuration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.roundToLong

internal object LanesSpec {
    const val PX_PER_SECOND = 24f
    const val EDGE = 16f
    const val BREAK_WIDTH = 72f
    const val BREAK_AFTER_MILLIS = 10_000L
    const val TICK_MILLIS = 5_000L
}

private val TickFormat = DateTimeFormatter.ofPattern("mm:ss")

internal enum class Lane { SCREENS, CLICKS, EVENTS }

internal data class LaneBar(val key: String, val start: Float, val end: Float, val label: String)

internal data class LaneMark(val key: String, val x: Float)

internal data class LaneBreak(val start: Float, val end: Float, val label: String)

internal data class LaneTick(val x: Float, val label: String)

internal data class Knot(val millis: Long, val x: Float)

/**
 * The whole session laid out left to right at [LanesSpec.PX_PER_SECOND], with every pause over
 * [LanesSpec.BREAK_AFTER_MILLIS] collapsed to a [LanesSpec.BREAK_WIDTH] break. Positions are in dp.
 */
internal class LanesStrip(
    val screens: List<LaneBar> = emptyList(),
    val clicks: List<LaneMark> = emptyList(),
    val events: List<LaneMark> = emptyList(),
    val breaks: List<LaneBreak> = emptyList(),
    val ticks: List<LaneTick> = emptyList(),
    private val knots: List<Knot> = emptyList(),
) {
    val width: Float = (knots.lastOrNull()?.x ?: 0f) + LanesSpec.EDGE

    fun xAt(millis: Long): Float {
        if (knots.isEmpty()) return LanesSpec.EDGE
        val after = knots.indexOfFirst { it.millis >= millis }
        return when {
            after == 0 -> knots.first().x - (knots.first().millis - millis) / 1000f * LanesSpec.PX_PER_SECOND
            after < 0 -> knots.last().x + (millis - knots.last().millis) / 1000f * LanesSpec.PX_PER_SECOND
            else -> interpolate(knots[after - 1], knots[after], millis)
        }
    }

    fun timeAt(x: Float): Long {
        if (knots.isEmpty()) return 0
        val after = knots.indexOfFirst { it.x >= x }
        return when {
            after == 0 -> knots.first().millis - ((knots.first().x - x) / LanesSpec.PX_PER_SECOND * 1000).roundToLong()
            after < 0 -> knots.last().millis + ((x - knots.last().x) / LanesSpec.PX_PER_SECOND * 1000).roundToLong()
            else -> {
                val from = knots[after - 1]
                val to = knots[after]
                if (to.x == from.x) from.millis else from.millis + ((x - from.x) / (to.x - from.x) * (to.millis - from.millis)).roundToLong()
            }
        }
    }

    private fun interpolate(from: Knot, to: Knot, millis: Long): Float =
        if (to.millis == from.millis) from.x else from.x + (to.x - from.x) * ((millis - from.millis).toFloat() / (to.millis - from.millis))

    companion object {
        fun of(events: List<CapturedEvent>): LanesStrip {
            val all = events.chronological()
            val knots = knots(all)
            val layout = LanesStrip(knots = knots)
            val screens = screenSpans(all).map { span ->
                LaneBar(span.firstEventId, layout.xAt(span.startMillis), span.endMillis?.let(layout::xAt) ?: layout.width, span.screen)
            }
            val breaks = knots.zipWithNext().filter { (from, to) -> to.millis - from.millis > LanesSpec.BREAK_AFTER_MILLIS }
                .map { (from, to) -> LaneBreak(from.x, to.x, formatDuration(to.millis - from.millis)) }
            val ticks = knots.zipWithNext().filter { (from, to) -> to.millis - from.millis <= LanesSpec.BREAK_AFTER_MILLIS }
                .flatMap { (from, to) -> ticksBetween(from.millis, to.millis) }
                .distinct()
                .map { LaneTick(layout.xAt(it), TickFormat.format(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()))) }
            return LanesStrip(
                knots = knots,
                screens = screens,
                clicks = all.filter { it.type == EventType.CLICK }.map { LaneMark(it.id, layout.xAt(it.timestampMillis)) },
                events = all.filter { it.type == EventType.OTHER }.map { LaneMark(it.id, layout.xAt(it.timestampMillis)) },
                breaks = breaks,
                ticks = ticks,
            )
        }

        private fun knots(chronological: List<CapturedEvent>): List<Knot> {
            val knots = mutableListOf<Knot>()
            chronological.map { it.timestampMillis }.distinct().forEach { millis ->
                val previous = knots.lastOrNull()
                knots += when {
                    previous == null -> Knot(millis, LanesSpec.EDGE)
                    millis - previous.millis > LanesSpec.BREAK_AFTER_MILLIS -> Knot(millis, previous.x + LanesSpec.BREAK_WIDTH)
                    else -> Knot(millis, previous.x + (millis - previous.millis) / 1000f * LanesSpec.PX_PER_SECOND)
                }
            }
            return knots
        }

        private fun ticksBetween(fromMillis: Long, toMillis: Long): List<Long> {
            val first = (fromMillis + LanesSpec.TICK_MILLIS - 1) / LanesSpec.TICK_MILLIS * LanesSpec.TICK_MILLIS
            return (first..toMillis step LanesSpec.TICK_MILLIS).toList()
        }
    }
}

internal fun lanesStrip(events: List<CapturedEvent>): LanesStrip = LanesStrip.of(events)

internal fun LanesStrip.markAt(lane: Lane, x: Float, tolerance: Float): String? = when (lane) {
    Lane.SCREENS -> screens.firstOrNull { x in it.start..it.end }?.key
    Lane.CLICKS -> clicks.nearest(x, tolerance)
    Lane.EVENTS -> events.nearest(x, tolerance)
}

private fun List<LaneMark>.nearest(x: Float, tolerance: Float): String? =
    filter { abs(it.x - x) <= tolerance }.minByOrNull { abs(it.x - x) }?.key
