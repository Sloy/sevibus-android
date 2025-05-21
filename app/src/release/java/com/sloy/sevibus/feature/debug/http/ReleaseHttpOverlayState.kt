package com.sloy.sevibus.feature.debug.http

import kotlinx.coroutines.flow.MutableStateFlow

class ReleaseHttpOverlayState : HttpOverlayState {
    override val isVisible = false
    override val items = MutableStateFlow<List<HttpOverlayItem>>(emptyList())
    override fun setVisibility(visible: Boolean) {
        // NA
    }

    override fun put(item: HttpOverlayItem) {
        // NA
    }
}
