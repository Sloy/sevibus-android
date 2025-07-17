package com.sloy.sevibus.feature.debug.location

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class LocationDebugModuleViewModel(private val dataSource: LocationDebugModuleDataSource) : ViewModel() {
    val state: StateFlow<LocationDebugModuleState> = dataSource.observeCurrentState()
    fun onFakeLocationChanged(isEnabled: Boolean) {
        dataSource.updateState(state.value.copy(isFakeLocationEnabled = isEnabled))
    }
}


@Serializable
data class LocationDebugModuleState(
    val isFakeLocationEnabled: Boolean = false,
)
