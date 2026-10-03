package com.sloy.debugmenu.network

import kotlinx.serialization.Serializable

@Serializable
data class NetworkDebugModuleState(
    val isHttpOverlayEnabled: Boolean = true,
    val isForceFailureEnabled: Boolean = false,
    val autoResetForceFailure: Boolean = false,
    val latencyPreset: LatencyPreset = LatencyPreset.Off,
    val hostOverride: String? = null,
)

fun NetworkDebugModuleState.isAnyFeatureActive(): Boolean =
    isHttpOverlayEnabled || isForceFailureEnabled || latencyPreset != LatencyPreset.Off || hostOverride != null
