package com.sloy.sevibus.feature.debug

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable

interface DebugModule {
    @Composable
    fun Component()
}

@Serializable
data class LocationDebugState(val isFakeLocation: Boolean = true)
