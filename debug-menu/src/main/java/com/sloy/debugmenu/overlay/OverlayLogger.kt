package com.sloy.debugmenu.overlay

import kotlinx.coroutines.flow.StateFlow
import kotlin.reflect.KClass

/**
 * Bus of items rendered by the debug overlay.
 */
interface OverlayLogger {
    val items: StateFlow<List<OverlayLoggerItem>>

    fun put(item: OverlayLoggerItem)

    fun clear(type: KClass<out OverlayLoggerItem>)
}
