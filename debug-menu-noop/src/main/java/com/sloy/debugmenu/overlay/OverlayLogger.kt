package com.sloy.debugmenu.overlay

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.reflect.KClass

interface OverlayLoggerItem {
    val id: String
    val autoHide: Boolean

    @Composable
    fun Content(modifier: Modifier)
}

interface OverlayLogger {
    val items: StateFlow<List<OverlayLoggerItem>>

    fun put(item: OverlayLoggerItem)

    fun clear(type: KClass<out OverlayLoggerItem>)
}

class NoopOverlayLogger : OverlayLogger {
    override val items: StateFlow<List<OverlayLoggerItem>> = MutableStateFlow(emptyList())

    override fun put(item: OverlayLoggerItem) = Unit

    override fun clear(type: KClass<out OverlayLoggerItem>) = Unit
}
