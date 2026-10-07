package com.sloy.sevibus.feature.debug.map

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.google.maps.android.compose.CameraPositionState
import com.sloy.sevibus.feature.debug.MapDebugOptions
import com.sloy.sevibus.feature.map.MapScreenState

@Composable
fun rememberMapDebugOptions(): MapDebugOptions = MapDebugOptions()

@Composable
fun MapDebugOverlay(
    options: MapDebugOptions,
    state: MapScreenState,
    cameraPositionState: CameraPositionState,
    contentPadding: PaddingValues,
    fitPadding: Dp,
    modifier: Modifier = Modifier,
) = Unit
