package com.sloy.sevibus.feature.debug.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthDebugModuleState(
    val placeholder: Boolean = false,
)