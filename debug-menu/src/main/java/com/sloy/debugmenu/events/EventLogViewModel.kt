package com.sloy.debugmenu.events

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

class EventLogViewModel(private val eventStore: EventStore) : ViewModel() {

    val events: StateFlow<List<CapturedEvent>> = eventStore.events

    fun onClearEvents() {
        eventStore.clear()
    }
}
