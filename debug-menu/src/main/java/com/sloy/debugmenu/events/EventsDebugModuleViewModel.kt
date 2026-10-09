package com.sloy.debugmenu.events

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

class EventsDebugModuleViewModel(
    private val dataSource: EventsDebugModuleDataSource,
    private val eventStore: EventStore,
) : ViewModel() {

    val state: StateFlow<EventsDebugModuleState> = dataSource.observeCurrentState()
    val events: StateFlow<List<CapturedEvent>> = eventStore.events

    fun onOverlayToggled(enabled: Boolean) {
        dataSource.updateState(dataSource.getCurrentState().copy(isOverlayEnabled = enabled))
    }

    fun onTimelineRailToggled(enabled: Boolean) {
        dataSource.updateState(dataSource.getCurrentState().copy(useTimelineRail = enabled))
    }

    fun onClearEvents() {
        eventStore.clear()
    }
}
