package com.sloy.debugmenu.events

import androidx.lifecycle.ViewModel
import com.sloy.debugmenu.overlay.OverlayLogger
import kotlinx.coroutines.flow.StateFlow

class EventsDebugModuleViewModel(
    private val dataSource: EventsDebugModuleDataSource,
    private val eventStore: EventStore,
    private val overlayLogger: OverlayLogger,
) : ViewModel() {

    val state: StateFlow<EventsDebugModuleState> = dataSource.observeCurrentState()
    val events: StateFlow<List<CapturedEvent>> = eventStore.events

    fun onOverlayToggled(enabled: Boolean) {
        dataSource.updateState(dataSource.getCurrentState().copy(isOverlayEnabled = enabled))
        if (!enabled) {
            overlayLogger.clear(EventOverlayLoggerItem::class)
        }
    }

    fun onClearEvents() {
        eventStore.clear()
    }
}
