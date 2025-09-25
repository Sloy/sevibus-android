package com.sloy.sevibus.feature.map

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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
import com.sloy.sevibus.infrastructure.extensions.koinInjectOnUI

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
