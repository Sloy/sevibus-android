package com.sloy.debugmenu.overlay

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface OverlayItem {
    val id: String
    val autoHide: Boolean

    @Composable
    fun Content(modifier: Modifier)
}

interface OverlayLoggerStateHolder {
    val items: StateFlow<List<OverlayItem>>
    val isVisible: StateFlow<Boolean>

    fun visibleWhen(flow: Flow<Boolean>)
    fun put(item: OverlayItem)
}

class NoopOverlayLoggerStateHolder : OverlayLoggerStateHolder {
    override val items: StateFlow<List<OverlayItem>> = MutableStateFlow(emptyList())
    override val isVisible: StateFlow<Boolean> = MutableStateFlow(false)

    override fun visibleWhen(flow: Flow<Boolean>) {}
    override fun put(item: OverlayItem) {}
}