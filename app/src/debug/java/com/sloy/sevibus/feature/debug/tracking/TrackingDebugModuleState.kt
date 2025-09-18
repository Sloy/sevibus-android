package com.sloy.sevibus.feature.debug.tracking

import kotlinx.serialization.Serializable

@Serializable
data class TrackingDebugModuleState(
    val isOverlayEnabled: Boolean = false,
)
