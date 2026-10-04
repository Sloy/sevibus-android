package com.sloy.debugmenu.testing

import com.sloy.debugmenu.overlay.OverlayLogger
import com.sloy.debugmenu.overlay.OverlayLoggerItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.reflect.KClass

class RecordingOverlayLogger : OverlayLogger {
    val putItems = mutableListOf<OverlayLoggerItem>()
    val clearedTypes = mutableListOf<KClass<out OverlayLoggerItem>>()

    override val items: StateFlow<List<OverlayLoggerItem>> = MutableStateFlow(emptyList())

    override fun put(item: OverlayLoggerItem) {
        putItems += item
    }

    override fun clear(type: KClass<out OverlayLoggerItem>) {
        clearedTypes += type
    }
}
