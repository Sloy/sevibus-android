package com.sloy.sevibus.feature.debug.tracking

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

class TrackingDebugModuleViewModel constructor(private val dataSource: TrackingDebugModuleDataSource) : ViewModel() {
    val state: StateFlow<TrackingDebugModuleState> = dataSource.observeCurrentState()
    fun onOverlayChanged(isEnabled: Boolean) {
        dataSource.updateState(state.value.copy(isOverlayEnabled = isEnabled))
    }
}
