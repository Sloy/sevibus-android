package com.sloy.debugmenu.overlay

import com.sloy.sevibus.feature.debug.network.overlay.OverlayItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Public API to interact with the overlay logger state and add new items.
 */
interface OverlayLoggerStateHolder {
    val items: StateFlow<List<OverlayItem>>
    val isVisible: StateFlow<Boolean>

    fun visibleWhen(flow: Flow<Boolean>)
    fun put(item: OverlayItem)
}
