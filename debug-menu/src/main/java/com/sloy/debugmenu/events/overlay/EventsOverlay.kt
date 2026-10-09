package com.sloy.debugmenu.events.overlay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sloy.debugmenu.events.CapturedEvent
import com.sloy.debugmenu.events.EventStore
import com.sloy.debugmenu.events.EventsDebugModuleDataSource

private const val CLOCK_IDLE_AFTER_MILLIS = 25_000L

/**
 * Live events overlay: the spring stack, or the timeline rail when enabled in the Events module.
 * It has no pointer input, so touches reach the app below. It is hidden from accessibility and only shows events tracked after it was turned on.
 */
@Composable
fun EventsOverlay(eventStore: EventStore, dataSource: EventsDebugModuleDataSource, modifier: Modifier = Modifier) {
    val state by dataSource.observeCurrentState().collectAsStateWithLifecycle()
    if (state.isOverlayEnabled) {
        val events by eventStore.events.collectAsStateWithLifecycle()
        val startedAt = rememberSaveable(state.useTimelineRail) { System.currentTimeMillis() }
        val recent = remember(events, startedAt) { events.filter { it.timestampMillis >= startedAt } }
        val now = rememberOverlayClock(recent)
        Box(
            modifier
                .fillMaxSize()
                .clearAndSetSemantics {},
        ) {
            if (state.useTimelineRail) EventsRailOverlay(recent, now) else EventsStackOverlay(recent, now)
        }
    }
}

@Composable
private fun rememberOverlayClock(events: List<CapturedEvent>): Long {
    val newest = events.maxOfOrNull { it.timestampMillis }
    return produceState(System.currentTimeMillis(), newest) {
        while (newest != null && System.currentTimeMillis() - newest <= CLOCK_IDLE_AFTER_MILLIS) {
            withFrameMillis { }
            value = System.currentTimeMillis()
        }
        value = System.currentTimeMillis()
    }.value
}
