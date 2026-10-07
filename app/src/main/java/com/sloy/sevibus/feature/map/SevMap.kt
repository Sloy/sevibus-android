package com.sloy.sevibus.feature.map

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.sloy.sevibus.domain.model.PositionBounds
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import com.sloy.sevibus.R
import com.sloy.sevibus.domain.model.SEVILLA_CAMERA_TARGET_BOUNDS
import com.sloy.sevibus.domain.model.SEVILLA_CENTER
import com.sloy.sevibus.domain.model.Stop
import com.sloy.sevibus.domain.model.isInsideSevilla
import com.sloy.sevibus.domain.model.toBounds
import com.sloy.sevibus.domain.model.toLatLng
import com.sloy.sevibus.domain.model.toLatLngBounds
import com.sloy.sevibus.feature.debug.map.MapDebugOverlay
import com.sloy.sevibus.feature.debug.map.rememberMapDebugOptions
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
    val debugOptions = rememberMapDebugOptions()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(SEVILLA_CENTER, ZoomLevel.Far.minimumLevel.toFloat())
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

    suspend fun centerMapOn(cameraUpdate: () -> CameraUpdate) {
        delay(20)
        while (!sheetState.isIdle) {
            delay(10)
        }
        cameraPositionState.animate(cameraUpdate(), MAP_CAMERA_ANIMATION_DURATION)
    }

    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    var mapSize by remember { mutableStateOf(IntSize.Zero) }
    val fitArea = PaddingValues(
        start = FIT_MARGIN_HORIZONTAL,
        end = FIT_MARGIN_HORIZONTAL,
        top = FIT_MARGIN_VERTICAL,
        bottom = contentPadding.calculateBottomPadding() + FIT_MARGIN_VERTICAL,
    )
    val viewport by rememberUpdatedState(
        MapViewport(
            width = mapSize.width.toFloat(),
            height = mapSize.height.toFloat(),
            cameraPadding = contentPadding.toEdgeInsets(density, layoutDirection),
            fitArea = fitArea.toEdgeInsets(density, layoutDirection),
            density = density.density,
        )
    )

    fun fitCamera(bounds: PositionBounds): CameraUpdate {
        val camera = CameraFit.fit(bounds, viewport, MIN_ZOOM, FIT_MAX_ZOOM)
        return CameraUpdateFactory.newLatLngZoom(camera.target.toLatLng(), camera.zoom)
    }

    val fitBounds = state.cameraFitBounds()
    if (state is MapScreenState.StopSelected) {
        LaunchedEffect(state.selectedStop) {
            centerMapOn { CameraUpdateFactory.newLatLngZoom(state.selectedStop.position.toLatLng(), ZoomLevel.Close.minimumLevel.toFloat()) }
        }
    } else if (fitBounds != null) {
        LaunchedEffect(fitBounds) {
            centerMapOn { fitCamera(fitBounds) }
        }
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
                minZoomPreference = MIN_ZOOM,
                isMyLocationEnabled = hasLocationPermission,
                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                    context,
                    if (isSystemInDarkTheme) R.raw.map_style_night else R.raw.map_style_default
                ),
                latLngBoundsForCameraTarget = SEVILLA_CAMERA_TARGET_BOUNDS
            )
        )
    }

    val locationSource = koinInjectOnUI<LocationSource>()
    Box(
        modifier
            .fillMaxSize()
            .onSizeChanged { mapSize = it }
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            uiSettings = mapUiSettings,
            properties = mapProperties,
            contentPadding = contentPadding,
            cameraPositionState = cameraPositionState,
            onMapClick = { onMapClick() },
            locationSource = locationSource,
        ) {
            val zoomLevel = ZoomLevel(cameraPositionState.position.zoom.toInt())
            val showBuses = cameraPositionState.position.zoom >= BUS_MARKERS_MIN_ZOOM
            MarkerLayersByState(state, zoomLevel, onStopSelected, showBuses, debugOptions)
            if (debugOptions.showFitBounds && fitBounds != null) {
                FitBoundsOutline(fitBounds.toLatLngBounds())
            }
        }
        MapDebugOverlay(debugOptions, state, cameraPositionState, contentPadding, fitArea)
    }
}

private fun MapScreenState.cameraFitBounds(): PositionBounds? = when (this) {
    is MapScreenState.LineSelected -> (lineStops.map { it.position } + path?.points.orEmpty()).toBounds()
    is MapScreenState.StopAndLineSelected -> selectedStops().map { it.position }.toBounds()
    else -> null
}

private fun PaddingValues.toEdgeInsets(density: Density, layoutDirection: LayoutDirection) = with(density) {
    EdgeInsets(
        left = calculateLeftPadding(layoutDirection).toPx(),
        top = calculateTopPadding().toPx(),
        right = calculateRightPadding(layoutDirection).toPx(),
        bottom = calculateBottomPadding().toPx(),
    )
}

@Composable
@GoogleMapComposable
private fun FitBoundsOutline(bounds: LatLngBounds) {
    Polygon(
        points = listOf(
            bounds.southwest,
            LatLng(bounds.southwest.latitude, bounds.northeast.longitude),
            bounds.northeast,
            LatLng(bounds.northeast.latitude, bounds.southwest.longitude),
        ),
        fillColor = Color.Transparent,
        strokeColor = Color(0xFF2196F3),
        strokeWidth = 4f,
        zIndex = Float.MAX_VALUE,
    )
}

private const val MIN_ZOOM = 11f
private const val FIT_MAX_ZOOM = 16f
private const val BUS_MARKERS_MIN_ZOOM = 13f
private val FIT_MARGIN_HORIZONTAL = 24.dp
private val FIT_MARGIN_VERTICAL = 8.dp
private const val MY_LOCATION_ZOOM = 17f
private const val MAP_CAMERA_ANIMATION_DURATION = 200
