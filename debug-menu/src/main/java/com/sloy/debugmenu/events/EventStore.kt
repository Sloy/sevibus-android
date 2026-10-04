package com.sloy.debugmenu.events

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory buffer of the latest captured events, newest first.
 */
class EventStore(private val capacity: Int = DEFAULT_CAPACITY) {
    private val mutableEvents = MutableStateFlow<List<CapturedEvent>>(emptyList())
    val events: StateFlow<List<CapturedEvent>> = mutableEvents

    fun add(event: CapturedEvent) {
        mutableEvents.update { current -> (listOf(event) + current).take(capacity) }
    }

    fun clear() {
        mutableEvents.value = emptyList()
    }
}

private const val DEFAULT_CAPACITY = 200
