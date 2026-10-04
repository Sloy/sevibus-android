package com.sloy.debugmenu.overlay

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

/**
 * Default [OverlayLogger]. Items are upserted by id and auto-hide items are removed after [autoHideDelayMillis].
 */
class OverlayLoggerImpl(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val autoHideDelayMillis: Long = AUTO_HIDE_DELAY_MILLIS,
) : OverlayLogger {

    private val mutableItems = MutableStateFlow<List<OverlayLoggerItem>>(emptyList())
    override val items: StateFlow<List<OverlayLoggerItem>> = mutableItems

    override fun put(item: OverlayLoggerItem) {
        mutableItems.update { current ->
            if (current.any { it.id == item.id }) {
                current.map { if (it.id == item.id) item else it }
            } else {
                current + item
            }
        }
        if (item.autoHide) {
            scope.launch {
                delay(autoHideDelayMillis)
                mutableItems.update { current -> current - item }
            }
        }
    }

    override fun clear(type: KClass<out OverlayLoggerItem>) {
        mutableItems.update { current -> current.filterNot { type.isInstance(it) } }
    }
}

private const val AUTO_HIDE_DELAY_MILLIS = 9_000L
