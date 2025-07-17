package com.sloy.sevibus.feature.debug

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocationDebugModule {

    companion object {
        private val _locationState = MutableStateFlow(LocationDebugState())
        val locationState = _locationState as StateFlow<LocationDebugState>
        fun setFakeLocation(isFakeLocation: Boolean) {
            _locationState.value = _locationState.value.copy(isFakeLocation = isFakeLocation)
        }
    }

}
