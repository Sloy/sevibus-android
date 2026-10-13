package com.sloy.debugmenu.events

import java.util.UUID

/**
 * Analytics event captured for debugging purposes.
 */
data class CapturedEvent(
    val name: String,
    val properties: Map<String, String> = emptyMap(),
    val timestampMillis: Long,
    val id: String = UUID.randomUUID().toString(),
) {
    val timestamp: String get() = timestampMillis.toClockTime()
    val type: EventType get() = EventType.of(name)
}

internal fun List<CapturedEvent>.chronological(): List<CapturedEvent> = asReversed().sortedBy { it.timestampMillis }
