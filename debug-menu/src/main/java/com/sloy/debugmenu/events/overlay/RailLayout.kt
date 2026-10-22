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
    const val ROOM_STAGGER_MILLIS = 60L
    const val ROOM_MILLIS = 250L
    const val POP_MILLIS = 420L
    const val LABEL_IN_MILLIS = 360L
    const val LABEL_FADE_MILLIS = 250L
    val WINDOW_MILLIS: Long = (RAIL_HEIGHT / PX_PER_SECOND * 1000).toLong() + REMOVE_AFTER_EXTRA_MILLIS
}

/**
 * One event on the rail at a given instant. Everything is derived from the clock, so the layout has no animation state.
 *
 * @param labelProgress label entrance, 0 hidden to 1 shown, used for its slide and scale
 * @param labelAlpha final label opacity, with its entrance, aging and the top fade
 */
internal data class RailMarker(
    val key: String,
    val name: String,
    val type: EventType,
    val y: Float,
    val markerScale: Float,
    val markerAlpha: Float,
    val labelProgress: Float,
    val labelAlpha: Float,
    val linkLength: Float,
) {
    val labelVisible: Boolean get() = labelAlpha > 0f
}

internal data class RailBand(val key: String, val top: Float, val bottom: Float, val alpha: Float)

internal data class RailFrame(val markers: List<RailMarker>, val bands: List<RailBand>)

/**
 * Lays out the rail at [nowMillis]. A new event first makes room, pushing the older ones up over [RailSpec.ROOM_MILLIS],
 * and only then pops in. Events arriving together make room [RailSpec.ROOM_STAGGER_MILLIS] apart.
 */
internal fun railFrame(events: List<CapturedEvent>, nowMillis: Long): RailFrame {
    val live = events.chronological().filter { it.timestampMillis <= nowMillis && nowMillis - it.timestampMillis <= RailSpec.WINDOW_MILLIS }
    val roomAts = roomTimes(live)
    val markers = ArrayList<RailMarker>(live.size)
    var newer: Placed? = null
    for (index in live.indices.reversed()) {
        val event = live[index]
        val ageMillis = nowMillis - event.timestampMillis
        val trueY = ageMillis / 1000f * RailSpec.PX_PER_SECOND
        val labelRoom = labelRoom(ageMillis)
        val y = newer?.let { maxOf(trueY, it.y + it.room * gap(labelRoom, it.labelRoom)) } ?: trueY
        val popAt = roomAts[index] + RailSpec.ROOM_MILLIS
        val sincePop = nowMillis - popAt
        val link = newer?.takeIf { it.popped && abs(it.timestampMillis - event.timestampMillis) < RailSpec.BURST_WINDOW_MILLIS }?.let { y - it.y } ?: 0f
        val edge = edgeAlpha(y)
        val labelProgress = labelProgress(sincePop, ageMillis)
        markers += RailMarker(
            key = event.id,
            name = event.name,
            type = event.type,
            y = y,
            markerScale = if (sincePop < 0) 0f else OverlayEasing.PopOut.transform(progress(sincePop, RailSpec.POP_MILLIS)),
            markerAlpha = edge,
            labelProgress = labelProgress,
            labelAlpha = labelFade(sincePop, ageMillis) * railLabelAlpha(ageMillis / 1000f) * edge,
            linkLength = link,
        )
        newer = Placed(
            y = y,
            room = OverlayEasing.CssEase.transform(progress(nowMillis - roomAts[index], RailSpec.ROOM_MILLIS)),
            labelRoom = labelRoom,
            popped = sincePop >= 0,
            timestampMillis = event.timestampMillis,
        )
    }
    return RailFrame(markers, screenBands(markers))
}

private class Placed(val y: Float, val room: Float, val labelRoom: Float, val popped: Boolean, val timestampMillis: Long)

private fun gap(labelRoom: Float, newerLabelRoom: Float): Float =
    RailSpec.DOT_GAP + (RailSpec.LABEL_GAP - RailSpec.DOT_GAP) * minOf(labelRoom, newerLabelRoom)

private fun labelRoom(ageMillis: Long): Float =
    1f - OverlayEasing.CssEase.transform(progress(ageMillis - labelHideAt(), RailSpec.LABEL_FADE_MILLIS))

private fun labelHideAt(): Long = (RailSpec.LABEL_SECONDS * 1000).toLong()

private fun labelProgress(sincePop: Long, ageMillis: Long): Float {
    if (sincePop < 0) return 0f
    val sinceHide = ageMillis - labelHideAt()
    if (sinceHide >= 0) return 1f - OverlayEasing.LabelOut.transform(progress(sinceHide, RailSpec.LABEL_IN_MILLIS))
    return OverlayEasing.LabelOut.transform(progress(sincePop, RailSpec.LABEL_IN_MILLIS))
}

private fun labelFade(sincePop: Long, ageMillis: Long): Float {
    if (sincePop < 0) return 0f
    val fadeIn = progress(sincePop, RailSpec.LABEL_FADE_MILLIS)
    val fadeOut = 1f - progress(ageMillis - labelHideAt(), RailSpec.LABEL_FADE_MILLIS)
    return minOf(fadeIn, fadeOut)
}

private fun progress(elapsedMillis: Long, durationMillis: Long): Float = (elapsedMillis.toFloat() / durationMillis).coerceIn(0f, 1f)

/**
 * Label opacity from its age: 1 for [RailSpec.HOLD_SECONDS], then down to 0.55 at [RailSpec.LABEL_SECONDS].
 */
internal fun railLabelAlpha(ageSeconds: Float): Float = when {
    ageSeconds <= RailSpec.HOLD_SECONDS -> 1f
    else -> 1f - RailSpec.LABEL_FADE * ((ageSeconds - RailSpec.HOLD_SECONDS) / (RailSpec.LABEL_SECONDS - RailSpec.HOLD_SECONDS)).coerceAtMost(1f)
}

private fun edgeAlpha(y: Float): Float = ((RailSpec.RAIL_HEIGHT - y) / RailSpec.EDGE_FADE).coerceIn(0f, 1f)

private fun roomTimes(chronological: List<CapturedEvent>): LongArray {
    val roomAts = LongArray(chronological.size)
    chronological.forEachIndexed { index, event ->
        roomAts[index] = if (index == 0) event.timestampMillis else maxOf(event.timestampMillis, roomAts[index - 1] + RailSpec.ROOM_STAGGER_MILLIS)
    }
    return roomAts
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
