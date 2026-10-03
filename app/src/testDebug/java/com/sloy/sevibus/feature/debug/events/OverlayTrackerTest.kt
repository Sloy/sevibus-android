package com.sloy.sevibus.feature.debug.events

import android.content.Context
import com.sloy.debugmenu.events.EventOverlayLoggerItem
import com.sloy.debugmenu.events.EventStore
import com.sloy.debugmenu.events.EventsDebugModuleDataSource
import com.sloy.debugmenu.events.EventsDebugModuleState
import com.sloy.debugmenu.overlay.OverlayLogger
import com.sloy.sevibus.infrastructure.analytics.SevEvent
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.single
import java.time.LocalTime

class OverlayTrackerTest {

    private val eventStore = EventStore()
    private val dataSource = EventsDebugModuleDataSource(mock<Context>(defaultAnswer = Mockito.RETURNS_DEEP_STUBS))
    private val overlayLogger: OverlayLogger = mock()
    private val tracker = OverlayTracker(eventStore, dataSource, overlayLogger, clock = { LocalTime.of(9, 5, 7) })

    @Test
    fun `maps event name properties and timestamp`() {
        tracker.track(TestEvent)

        expectThat(eventStore.events.value).single().and {
            get { name }.isEqualTo("Test Clicked")
            get { properties }.isEqualTo(mapOf("stopId" to "42", "isSelected" to "true"))
            get { timestamp }.isEqualTo("09:05:07")
        }
    }

    @Test
    fun `does not show overlay when disabled`() {
        tracker.track(TestEvent)

        verify(overlayLogger, never()).put(any())
    }

    @Test
    fun `shows overlay when enabled`() {
        dataSource.updateState(EventsDebugModuleState(isOverlayEnabled = true))

        tracker.track(TestEvent)

        val captor = argumentCaptor<EventOverlayLoggerItem>()
        verify(overlayLogger).put(captor.capture())
        expectThat(captor.firstValue.event.name).isEqualTo("Test Clicked")
    }

    private object TestEvent : SevEvent(
        "Test Clicked",
        "stopId" to 42,
        "isSelected" to true,
        "missing" to null,
    )
}
