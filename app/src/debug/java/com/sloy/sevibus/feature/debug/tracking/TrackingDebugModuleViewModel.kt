package com.sloy.sevibus.feature.debug.tracking

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class TrackingDebugModuleViewModel @Inject constructor(private val dataSource: TrackingDebugModuleDataSource) : ViewModel() {
    val state: StateFlow<TrackingDebugModuleState> = dataSource.observeCurrentState()
    fun onOverlayChanged(isEnabled: Boolean) {
        dataSource.updateState(state.value.copy(isOverlayEnabled = isEnabled))
    }
}
