package com.sloy.sevibus.feature.debug.events

import com.sloy.debugmenu.events.CapturedEvent
import com.sloy.debugmenu.events.EventStore
import com.sloy.sevibus.infrastructure.analytics.SevEvent
import com.sloy.sevibus.infrastructure.analytics.Tracker

class OverlayTracker(
    private val eventStore: EventStore,
    private val clock: () -> Long = System::currentTimeMillis,
) : Tracker {
    override fun track(event: SevEvent) {
        eventStore.add(event.toCapturedEvent(clock()))
    }
}

internal fun SevEvent.toCapturedEvent(timestampMillis: Long): CapturedEvent = CapturedEvent(
    name = name,
    properties = properties.mapNotNull { (key, value) -> value?.let { key to it.toString() } }.toMap(),
    timestampMillis = timestampMillis,
)
