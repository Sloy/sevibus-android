package com.sloy.sevibus.feature.debug.events

import com.sloy.debugmenu.events.EventStore
import com.sloy.sevibus.infrastructure.analytics.SevEvent
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.single

class OverlayTrackerTest {

    private val eventStore = EventStore()
    private val tracker = OverlayTracker(eventStore, clock = { 1_791_555_661_050 })

    @Test
    fun `maps event name properties and timestamp`() {
        tracker.track(TestEvent)

        expectThat(eventStore.events.value).single().and {
            get { name }.isEqualTo("Test Clicked")
            get { properties }.isEqualTo(mapOf("stopId" to "42", "isSelected" to "true"))
            get { timestampMillis }.isEqualTo(1_791_555_661_050)
        }
    }

    private object TestEvent : SevEvent(
        "Test Clicked",
        "stopId" to 42,
        "isSelected" to true,
        "missing" to null,
    )
}
