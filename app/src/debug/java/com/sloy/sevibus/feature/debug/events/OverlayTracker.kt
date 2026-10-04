package com.sloy.sevibus.feature.debug.events

import com.sloy.debugmenu.events.CapturedEvent
import com.sloy.debugmenu.events.EventOverlayLoggerItem
import com.sloy.debugmenu.events.EventStore
import com.sloy.debugmenu.events.EventsDebugModuleDataSource
import com.sloy.debugmenu.overlay.OverlayLogger
import com.sloy.sevibus.infrastructure.analytics.SevEvent
import com.sloy.sevibus.infrastructure.analytics.Tracker
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class OverlayTracker(
    private val eventStore: EventStore,
    private val eventsDataSource: EventsDebugModuleDataSource,
    private val overlayLogger: OverlayLogger,
    private val clock: () -> LocalTime = { LocalTime.now() },
) : Tracker {
    override fun track(event: SevEvent) {
        val capturedEvent = event.toCapturedEvent(clock())
        eventStore.add(capturedEvent)
        if (eventsDataSource.getCurrentState().isOverlayEnabled) {
            overlayLogger.put(EventOverlayLoggerItem(capturedEvent))
        }
    }
}

internal fun SevEvent.toCapturedEvent(time: LocalTime): CapturedEvent = CapturedEvent(
    name = name,
    properties = properties.mapNotNull { (key, value) -> value?.let { key to it.toString() } }.toMap(),
    timestamp = time.format(TIMESTAMP_FORMAT),
)

private val TIMESTAMP_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
