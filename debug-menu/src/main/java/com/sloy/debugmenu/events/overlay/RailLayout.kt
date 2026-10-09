package com.sloy.debugmenu.events.overlay

import com.sloy.debugmenu.events.CapturedEvent
import com.sloy.debugmenu.events.EventType
import com.sloy.debugmenu.events.chronological
import kotlin.math.abs

internal object RailSpec {
    const val PX_PER_SECOND = 31f
    const val RAIL_HEIGHT = 540f
    const val LABEL_SECONDS = 9f
    const val HOLD_SECONDS = 2f
    const val LABEL_FADE = 0.45f
    const val LABEL_GAP = 26f
    const val DOT_GAP = 7f
    const val BURST_WINDOW_MILLIS = 150L
    const val EDGE_FADE = 90f
    const val BAND_EXTENT = 10f
    const val REMOVE_AFTER_EXTRA_MILLIS = 1_500L
    const val POP_DELAY_MILLIS = 40L
    const val POP_STAGGER_MILLIS = 60L
    const val TICK_SECONDS = 5
    const val TICK_MARGIN = 10f
    val WINDOW_MILLIS: Long = (RAIL_HEIGHT / PX_PER_SECOND * 1000).toLong() + REMOVE_AFTER_EXTRA_MILLIS
}

internal data class RailMarker(
    val key: String,
    val name: String,
    val type: EventType,
    val y: Float,
    val trueY: Float,
    val labelVisible: Boolean,
    val labelAlpha: Float,
    val markerAlpha: Float,
    val linkLength: Float,
    val popAt: Long,
)

internal data class RailBand(val key: String, val top: Float, val bottom: Float, val alpha: Float)

internal data class RailTick(val y: Float, val label: String)

internal data class RailFrame(val markers: List<RailMarker>, val bands: List<RailBand>, val ticks: List<RailTick>)

internal fun railFrame(events: List<CapturedEvent>, nowMillis: Long): RailFrame {
    val live = events.chronological().filter { it.timestampMillis <= nowMillis && nowMillis - it.timestampMillis <= RailSpec.WINDOW_MILLIS }
    val popAts = popTimes(live)
    val markers = ArrayList<RailMarker>(live.size)
    var newer: RailMarker? = null
    var newerTimestamp = 0L
    for (index in live.indices.reversed()) {
        val event = live[index]
        val ageSeconds = (nowMillis - event.timestampMillis) / 1000f
        val trueY = ageSeconds * RailSpec.PX_PER_SECOND
        val labelVisible = ageSeconds < RailSpec.LABEL_SECONDS
        val y = newer?.let { maxOf(trueY, it.y + if (labelVisible && it.labelVisible) RailSpec.LABEL_GAP else RailSpec.DOT_GAP) } ?: trueY
        val link = newer?.takeIf { abs(newerTimestamp - event.timestampMillis) < RailSpec.BURST_WINDOW_MILLIS }?.let { y - it.y } ?: 0f
        val marker = RailMarker(
            key = event.id,
            name = event.name,
            type = event.type,
            y = y,
            trueY = trueY,
            labelVisible = labelVisible,
            labelAlpha = railLabelAlpha(ageSeconds),
            markerAlpha = edgeAlpha(y),
            linkLength = link,
            popAt = popAts[index],
        )
        markers += marker
        newer = marker
        newerTimestamp = event.timestampMillis
    }
    return RailFrame(markers, screenBands(markers), ticks())
}

internal fun railLabelAlpha(ageSeconds: Float): Float = when {
    ageSeconds >= RailSpec.LABEL_SECONDS -> 0f
    ageSeconds <= RailSpec.HOLD_SECONDS -> 1f
    else -> 1f - RailSpec.LABEL_FADE * (ageSeconds - RailSpec.HOLD_SECONDS) / (RailSpec.LABEL_SECONDS - RailSpec.HOLD_SECONDS)
}

private fun edgeAlpha(y: Float): Float = ((RailSpec.RAIL_HEIGHT - y) / RailSpec.EDGE_FADE).coerceIn(0f, 1f)

private fun popTimes(chronological: List<CapturedEvent>): LongArray {
    val popAts = LongArray(chronological.size)
    chronological.forEachIndexed { index, event ->
        val earliest = event.timestampMillis + RailSpec.POP_DELAY_MILLIS
        popAts[index] = if (index == 0) earliest else maxOf(earliest, popAts[index - 1] + RailSpec.POP_STAGGER_MILLIS)
    }
    return popAts
}

private fun screenBands(newestFirst: List<RailMarker>): List<RailBand> {
    val groups = mutableListOf<Triple<String, Float, Float>>()
    for (marker in newestFirst.asReversed()) {
        if (marker.type == EventType.VIEW) {
            groups += Triple(marker.key, marker.y, marker.y)
        } else if (groups.isNotEmpty()) {
            groups[groups.lastIndex] = groups.last().copy(third = marker.y)
        }
    }
    return groups.map { (key, top, bottom) ->
        RailBand(key, top + RailSpec.BAND_EXTENT, bottom - RailSpec.BAND_EXTENT, edgeAlpha(top))
    }
}

private fun ticks(): List<RailTick> =
    generateSequence(RailSpec.TICK_SECONDS) { it + RailSpec.TICK_SECONDS }
        .map { seconds -> RailTick(seconds * RailSpec.PX_PER_SECOND, "${seconds}s") }
        .takeWhile { it.y < RailSpec.RAIL_HEIGHT - RailSpec.TICK_MARGIN }
        .toList()
