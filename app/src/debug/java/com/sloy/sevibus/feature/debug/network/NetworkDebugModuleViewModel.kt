package com.sloy.sevibus.modules.tracking

import androidx.lifecycle.ViewModel
import com.sloy.debugmenu.overlay.OverlayLoggerStateHolder
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

class NetworkDebugModuleViewModel(
    private val dataSource: NetworkDebugModuleDataSource,
    private val overlayLoggerStateHolder: OverlayLoggerStateHolder
) : ViewModel() {
    val state: StateFlow<NetworkDebugModuleState> = dataSource.observeCurrentState()

    init {
        overlayLoggerStateHolder.visibleWhen(dataSource.observeCurrentState().map { it.isOverlayEnabled })
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
