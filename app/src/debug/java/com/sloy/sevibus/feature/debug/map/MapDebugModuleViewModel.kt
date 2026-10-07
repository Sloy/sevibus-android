package com.sloy.sevibus.feature.debug.map

import androidx.lifecycle.ViewModel
import com.sloy.sevibus.feature.debug.MapDebugOptions
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class MapDebugModuleViewModel(private val dataSource: MapDebugModuleDataSource) : ViewModel() {
    val state: StateFlow<MapDebugModuleState> = dataSource.observeCurrentState()

    fun onFakeLocationSelected(fakeLocation: FakeLocation?) = update { copy(fakeLocation = fakeLocation) }
    fun onShowMapStateToggled(enabled: Boolean) = update { copy(showMapState = enabled) }
    fun onShowCameraToggled(enabled: Boolean) = update { copy(showCamera = enabled) }
    fun onShowVisibleAreaToggled(enabled: Boolean) = update { copy(showVisibleArea = enabled) }
    fun onShowFitBoundsToggled(enabled: Boolean) = update { copy(showFitBounds = enabled) }
    fun onShowStopsToggled(enabled: Boolean) = update { copy(showStops = enabled) }
    fun onShowBusesToggled(enabled: Boolean) = update { copy(showBuses = enabled) }

    private fun update(change: MapDebugModuleState.() -> MapDebugModuleState) {
        dataSource.updateState(state.value.change())
    }
}

@Serializable
data class MapDebugModuleState(
    val fakeLocation: FakeLocation? = null,
    val showMapState: Boolean = false,
    val showCamera: Boolean = false,
    val showVisibleArea: Boolean = false,
    val showFitBounds: Boolean = false,
    val showStops: Boolean = true,
    val showBuses: Boolean = true,
) {
    val hasChanges: Boolean
        get() = this != MapDebugModuleState()

    fun toOptions() = MapDebugOptions(
        showStops = showStops,
        showBuses = showBuses,
        showMapState = showMapState,
        showCamera = showCamera,
        showVisibleArea = showVisibleArea,
        showFitBounds = showFitBounds,
    )
}
