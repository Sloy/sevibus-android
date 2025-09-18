package com.sloy.sevibus.feature.debug.tracking

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TrackingDebugModuleDataSource(context: Context) {
    fun observeCurrentState(): StateFlow<TrackingDebugModuleState> = MutableStateFlow(TrackingDebugModuleState())
}

data class TrackingDebugModuleState(
    val isOverlayEnabled: Boolean = false,
)
