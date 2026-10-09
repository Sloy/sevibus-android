package com.sloy.debugmenu.events

import com.sloy.debugmenu.testing.testContext
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEmpty
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class EventsDebugModuleViewModelTest {

    private val dataSource = EventsDebugModuleDataSource(testContext())
    private val eventStore = EventStore()
    private val viewModel = EventsDebugModuleViewModel(dataSource, eventStore)

    @Test
    fun `timeline rail is off by default`() {
        expectThat(dataSource.getCurrentState().useTimelineRail).isFalse()
    }

    @Test
    fun `toggling the timeline rail persists it`() {
        viewModel.onTimelineRailToggled(true)
        expectThat(dataSource.getCurrentState().useTimelineRail).isTrue()
    }

    @Test
    fun `toggling the overlay keeps the rail choice`() {
        viewModel.onTimelineRailToggled(true)
        viewModel.onOverlayToggled(true)
        viewModel.onOverlayToggled(false)
        expectThat(dataSource.getCurrentState().isOverlayEnabled).isFalse()
        expectThat(dataSource.getCurrentState().useTimelineRail).isTrue()
    }

    @Test
    fun `clear empties the store`() {
        eventStore.add(CapturedEvent("A", timestampMillis = 0))
        viewModel.onClearEvents()
        expectThat(eventStore.events.value).isEmpty()
    }
}
