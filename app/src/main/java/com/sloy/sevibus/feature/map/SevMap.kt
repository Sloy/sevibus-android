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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.sloy.sevibus.R
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.SEVILLA_BOUNDS
import com.sloy.sevibus.domain.model.Stop
import com.sloy.sevibus.domain.model.isInsideSevilla
import com.sloy.sevibus.domain.model.toBounds
import com.sloy.sevibus.domain.model.toLatLng
import com.sloy.sevibus.domain.model.toLatLngBounds
import com.sloy.sevibus.feature.map.layers.MarkerLayersByState
import com.sloy.sevibus.infrastructure.EventCollector
import com.sloy.sevibus.infrastructure.extensions.koinInjectOnUI
import com.sloy.sevibus.infrastructure.location.LocationService
import com.sloy.sevibus.infrastructure.location.NoopLocationService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun SevMap(
    state: MapScreenState,
    hasLocationPermission: Boolean,
    onStopSelected: (Stop) -> Unit,
    onMapClick: () -> Unit,
    contentPadding: PaddingValues,
    sheetState: com.composables.core.BottomSheetState,
    locationButtonClickFlow: SharedFlow<Unit>,
    onCameraPositionChanged: (LatLng) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locationService: LocationService = koinInjectOnUI() ?: NoopLocationService
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(Stubs.locationInitial.toLatLng(), ZoomLevel.Far.minimumLevel.toFloat())
    }

    LaunchedEffect(hasLocationPermission) {
        locationService.obtainCurrentLocation()?.toLatLng()?.let { userLocation ->
            if (userLocation.isInsideSevilla()) {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOCATION_ZOOM))
            }
        }
    }

    LaunchedEffect(cameraPositionState.isMoving, cameraPositionState.cameraMoveStartedReason) {
        if (cameraPositionState.isMoving && cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE) {
            onMapClick()
        }
    }

    suspend fun centerMapOn(cameraUpdate: CameraUpdate?) {
        if (cameraUpdate == null) return
        delay(20)
        while (!sheetState.isIdle) {
            delay(10)
        }
        cameraPositionState.animate(cameraUpdate, MAP_CAMERA_ANIMATION_DURATION)
    }

    val density = LocalDensity.current
    val boundsPadding = with(density) { 40.dp.toPx() }.toInt()

    when (state) {
        is MapScreenState.StopSelected -> {
            LaunchedEffect(state.selectedStop) {
                centerMapOn(CameraUpdateFactory.newLatLngZoom(state.selectedStop.position.toLatLng(), ZoomLevel.Close.minimumLevel.toFloat()))
            }
        }

        is MapScreenState.LineSelected -> {
            LaunchedEffect(state.lineStops) {
                centerMapOn(CameraUpdateFactory.newLatLngBounds(state.lineStops.toBounds().toLatLngBounds(), boundsPadding))
            }
        }

        is MapScreenState.StopAndLineSelected -> {
            LaunchedEffect(state.selectedStop) {
                centerMapOn(CameraUpdateFactory.newLatLngBounds(state.selectedStops().toBounds().toLatLngBounds(), boundsPadding))
            }
        }

        else -> {}
    }

    EventCollector(locationButtonClickFlow) {
        locationService.obtainCurrentLocation()?.toLatLng()?.let { userLocation ->
            if (userLocation.isInsideSevilla()) {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOCATION_ZOOM))
            }
        }
    }

    LaunchedEffect(cameraPositionState.position) {
        onCameraPositionChanged(cameraPositionState.position.target)
    }

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

private const val MY_LOCATION_ZOOM = 17f
private const val MAP_CAMERA_ANIMATION_DURATION = 200
