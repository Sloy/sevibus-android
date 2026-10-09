package com.sloy.debugmenu.events.viewer

import com.sloy.debugmenu.events.CapturedEvent
import com.sloy.debugmenu.events.EventType
import com.sloy.debugmenu.events.chronological
import com.sloy.debugmenu.events.formatDelta
import com.sloy.debugmenu.events.formatDuration

internal const val SESSION_SUMMARY = "Session Summary"
private const val GAP_MILLIS = 10_000L

internal val CapturedEvent.isSessionSummary: Boolean get() = name == SESSION_SUMMARY

internal sealed interface ViewerItem {
    val key: String
}

internal data class EventItem(val event: CapturedEvent, val delta: String, val inBand: Boolean = false) : ViewerItem {
    override val key: String get() = event.id
}

internal data class SessionItem(val event: CapturedEvent) : ViewerItem {
    override val key: String get() = event.id
}

internal data class GapItem(override val key: String, val label: String) : ViewerItem

internal data class BandHeaderItem(override val key: String, val screen: String, val startMillis: Long, val duration: String?) : ViewerItem

internal data class BandEndItem(override val key: String) : ViewerItem

internal fun timelineItems(events: List<CapturedEvent>, query: String = ""): List<ViewerItem> {
    val all = events.chronological()
    val deltas = deltas(all)
    val visible = all.filter { it.matches(query) }
    val items = mutableListOf<ViewerItem>()
    for (index in visible.indices.reversed()) {
        val event = visible[index]
        if (event.isSessionSummary) {
            items += SessionItem(event)
            continue
        }
        items += EventItem(event, deltas.getValue(event.id))
        val older = visible.subList(0, index).lastOrNull { !it.isSessionSummary } ?: continue
        gapBetween(older, event, all)?.let { items += it }
    }
    return items
}

internal fun deltas(chronological: List<CapturedEvent>): Map<String, String> {
    var previous: CapturedEvent? = null
    return buildMap {
        chronological.filterNot { it.isSessionSummary }.forEach { event ->
            put(event.id, previous?.let { formatDelta(event.timestampMillis - it.timestampMillis) } ?: "first")
            previous = event
        }
    }
}

private fun gapBetween(older: CapturedEvent, newer: CapturedEvent, all: List<CapturedEvent>): GapItem? {
    val gap = newer.timestampMillis - older.timestampMillis
    if (gap < GAP_MILLIS) return null
    val inBackground = all.any { it.isSessionSummary && it.timestampMillis in older.timestampMillis..newer.timestampMillis }
    return GapItem("gap-${newer.id}", "· ${formatDuration(gap)} ${if (inBackground) "in background" else "quiet"} ·")
}

private sealed interface Block

private class Band(val key: String, val screen: String, val startMillis: Long, val events: MutableList<CapturedEvent>) : Block {
    var endMillis: Long? = null
}

private class SessionBlock(val event: CapturedEvent) : Block

private class LooseBlock(val event: CapturedEvent) : Block

internal fun journeyItems(events: List<CapturedEvent>, query: String = ""): List<ViewerItem> {
    val all = events.chronological()
    val deltas = deltas(all)
    val blocks = buildBlocks(all)
    val items = mutableListOf<ViewerItem>()
    for (index in blocks.indices.reversed()) {
        when (val block = blocks[index]) {
            is SessionBlock -> if (block.event.matches(query)) items += SessionItem(block.event)
            is LooseBlock -> if (block.event.matches(query)) items += EventItem(block.event, deltas.getValue(block.event.id))
            is Band -> {
                val rows = block.events.filter { it.matches(query) }
                if (rows.isEmpty()) continue
                items += BandHeaderItem("band-${block.key}", block.screen, block.startMillis, block.endMillis?.let { formatDuration(it - block.startMillis) })
                rows.asReversed().forEach { items += EventItem(it, deltas.getValue(it.id), inBand = true) }
                items += BandEndItem("band-end-${block.key}")
                backgroundGap(block, blocks.getOrNull(index - 1), all)?.let { items += it }
            }
        }
    }
    return items
}

private fun buildBlocks(chronological: List<CapturedEvent>): List<Block> {
    val blocks = mutableListOf<Block>()
    var current: Band? = null
    for (event in chronological) {
        when {
            event.isSessionSummary -> {
                current?.endMillis = event.timestampMillis
                current = null
                blocks += SessionBlock(event)
            }
            event.type == EventType.VIEW -> {
                current?.endMillis = event.timestampMillis
                current = Band(event.id, event.name.removeSuffix(" Viewed"), event.timestampMillis, mutableListOf(event)).also { blocks += it }
            }
            else -> {
                val band = current
                val lastBlock = blocks.lastOrNull()
                when {
                    band != null -> band.events += event
                    lastBlock is SessionBlock -> {
                        current = Band("resumed-${event.id}", resumedScreen(lastBlock.event), event.timestampMillis, mutableListOf(event)).also { blocks += it }
                    }
                    else -> blocks += LooseBlock(event)
                }
            }
        }
    }
    return blocks
}

private fun resumedScreen(sessionSummary: CapturedEvent): String {
    val lastScreen = sessionSummary.properties["lastScreen"] ?: return "Resumed"
    return lastScreen.split('_').joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } } + " (resumed)"
}

private fun backgroundGap(band: Band, previous: Block?, all: List<CapturedEvent>): GapItem? {
    if (previous !is SessionBlock) return null
    val lastBefore = all.lastOrNull { !it.isSessionSummary && it.timestampMillis < previous.event.timestampMillis } ?: return null
    return GapItem("gap-${band.key}", "· ${formatDuration(band.startMillis - lastBefore.timestampMillis)} in background ·")
}
