package com.sloy.sevibus.feature.debug.location

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class LocationDebugModuleViewModel(private val dataSource: LocationDebugModuleDataSource) : ViewModel() {
    val state: StateFlow<LocationDebugModuleState> = dataSource.observeCurrentState()

    fun onFakeLocationSelected(fakeLocation: FakeLocation?) {
        dataSource.updateState(state.value.copy(fakeLocation = fakeLocation))
    }
}

@Serializable
data class LocationDebugModuleState(
    val fakeLocation: FakeLocation? = null,
)
