package com.sloy.sevibus.modules.tracking

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class NetworkDebugModuleDataSource {
    fun observeCurrentState(): StateFlow<NetworkDebugModuleState> = MutableStateFlow(NetworkDebugModuleState())
}

@Serializable
data class NetworkDebugModuleState(
    val isOverlayEnabled: Boolean = false,
)