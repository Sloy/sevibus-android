package com.sloy.sevibus.feature.map

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.sloy.sevibus.R
import com.sloy.sevibus.domain.model.SEVILLA_BOUNDS
import com.sloy.sevibus.domain.model.Stop
import com.sloy.sevibus.feature.map.layers.MarkerLayersByState
import com.sloy.sevibus.infrastructure.extensions.centerStops
import com.sloy.sevibus.infrastructure.extensions.koinInjectOnUI
import com.sloy.sevibus.infrastructure.extensions.zoomInto

@Composable
fun SevMap(
    state: MapScreenState,
    cameraPositionState: CameraPositionState,
    hasLocationPermission: Boolean,
    onStopSelected: (Stop) -> Unit,
    onMapClick: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = false,
                mapToolbarEnabled = false,
                compassEnabled = true,
                myLocationButtonEnabled = false,
            )
        )
    }
    val context = LocalContext.current
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val mapProperties by remember(hasLocationPermission) {
        mutableStateOf(
            MapProperties(
                minZoomPreference = 13f,
                isMyLocationEnabled = hasLocationPermission,
                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                    context,
                    if (isSystemInDarkTheme) R.raw.map_style_night else R.raw.map_style_default
                ),
                latLngBoundsForCameraTarget = SEVILLA_BOUNDS
            )
        )
    }

    when (state) {
        is MapScreenState.LineSelected -> {
            LaunchedEffect(state.lineStops) {
                cameraPositionState.centerStops(state.lineStops)
            }
        }

        is MapScreenState.StopAndLineSelected -> {
            val selectedStop = state.selectedStop
            val lineStops = state.lineSelectedState.lineStops
            val stopIndex = lineStops.indexOf(selectedStop)
            val nextStop = lineStops.getOrNull(stopIndex + 1)
            val previousStop = lineStops.getOrNull(stopIndex - 1)

            val stopsToZoom = listOf(previousStop, selectedStop, nextStop).filterNotNull()
            LaunchedEffect(stopsToZoom) {
                cameraPositionState.centerStops(stopsToZoom)
            }
        }

        is MapScreenState.StopSelected -> {
            LaunchedEffect(state.selectedStop) {
                cameraPositionState.zoomInto(state.selectedStop.position)
            }
        }

        else -> {}
    }

    val locationSource = koinInjectOnUI<LocationSource>()
    GoogleMap(
        modifier = modifier.fillMaxSize(),
        uiSettings = mapUiSettings,
        properties = mapProperties,
        contentPadding = contentPadding,
        cameraPositionState = cameraPositionState,
        onMapClick = { onMapClick() },
        locationSource = locationSource,
    ) {
        val zoomLevel = ZoomLevel(cameraPositionState.position.zoom.toInt())
        MarkerLayersByState(state, zoomLevel, onStopSelected)
    }
}
