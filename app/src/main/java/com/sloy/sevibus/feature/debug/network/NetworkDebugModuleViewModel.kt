package com.n26.debug.modules.tracking

import androidx.lifecycle.ViewModel
import com.sloy.sevibus.feature.debug.network.overlay.OverlayLoggerStateHolder
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

class NetworkDebugModuleViewModel(private val dataSource: NetworkDebugModuleDataSource) : ViewModel() {
    val state: StateFlow<NetworkDebugModuleState> = dataSource.observeCurrentState()

    init {
        OverlayLoggerStateHolder.visibleWhen(dataSource.observeCurrentState().map { it.isOverlayEnabled })
    }

    fun onOverlayChanged(isEnabled: Boolean) {
        dataSource.updateState(state.value.copy(isOverlayEnabled = isEnabled))
    }
}

@Serializable
data class NetworkDebugModuleState(
    val isOverlayEnabled: Boolean = true,
    //TODO environment selection
)
