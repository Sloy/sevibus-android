package com.sloy.sevibus.feature.map

import android.Manifest
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.Stop
import com.sloy.sevibus.domain.model.isInsideSevilla
import com.sloy.sevibus.domain.model.toLatLng
import com.sloy.sevibus.infrastructure.EventCollector
import com.sloy.sevibus.infrastructure.FeatureFlags
import com.sloy.sevibus.infrastructure.extensions.isApproximatelyEqualTo
import com.sloy.sevibus.infrastructure.extensions.koinInjectOnUI
import com.sloy.sevibus.infrastructure.extensions.performHapticReject
import com.sloy.sevibus.infrastructure.extensions.rememberPermissionStateOnUI
import com.sloy.sevibus.infrastructure.location.LocationService
import com.sloy.sevibus.infrastructure.location.NoopLocationService
import com.sloy.sevibus.ui.animation.animateShake
import com.sloy.sevibus.ui.components.CustomFab
import com.sloy.sevibus.ui.preview.ScreenPreview
import com.sloy.sevibus.ui.snackbar.LocalSnackbarHostState
import com.sloy.sevibus.ui.theme.SevTheme
import kotlinx.coroutines.launch
import okhttp3.internal.format
import org.koin.androidx.compose.koinViewModel


@Composable
fun MapScreen(
    contentPadding: PaddingValues,
    onStopSelected: (stop: Stop) -> Unit,
    onMapClick: () -> Unit,
) {
    if (!LocalView.current.isInEditMode) {
        val mapViewModel: MapViewModel = koinViewModel()
        val state by mapViewModel.state.collectAsStateWithLifecycle()
        MapScreen(state, contentPadding, onStopSelected, onMapClick)
        val snackbarState = LocalSnackbarHostState.current
        EventCollector(mapViewModel.events) { event ->
            when (event) {
                is MapScreenEvent.Error -> snackbarState.showSnackbar(event.message, withDismissAction = true)
            }
        }
    } else {
        MapScreen(MapScreenState.Initial, contentPadding, onStopSelected, onMapClick)
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun MapScreen(
    state: MapScreenState,
    contentPadding: PaddingValues,
    onStopSelected: (stop: Stop) -> Unit,
    onMapClick: () -> Unit,
) {
    val locationService: LocationService = koinInjectOnUI() ?: NoopLocationService
    val locationPermissionState = rememberPermissionStateOnUI(Manifest.permission.ACCESS_FINE_LOCATION)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(Stubs.locationInitial.toLatLng(), ZoomLevel.Far.minimumLevel.toFloat())
    }
    LaunchedEffect(locationPermissionState?.status?.isGranted) {
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

    MapUI(state, cameraPositionState, locationPermissionState, locationService, contentPadding, onStopSelected, onMapClick)
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun MapUI(
    state: MapScreenState,
    cameraPositionState: CameraPositionState,
    locationPermissionState: PermissionState?,
    locationService: LocationService,
    contentPadding: PaddingValues,
    onStopSelected: (stop: Stop) -> Unit,
    onMapClick: () -> Unit
) {
    val shakeAnim = remember { Animatable(0f) }

    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer { translationX = shakeAnim.value }) {
        if (FeatureFlags.showMapDebugInfo) {
            DebugInfo(
                state, cameraPositionState,
                Modifier
                    .zIndex(1f)
                    .padding(contentPadding)
            )
        }
        LocationButton(
            locationPermissionState, cameraPositionState, locationService,
            onReject = {
                shakeAnim.animateShake()
            },
            Modifier
                .zIndex(1f)
                .padding(contentPadding)
        )

        SevMap(
            state = state,
            cameraPositionState = cameraPositionState,
            hasLocationPermission = locationPermissionState?.status?.isGranted ?: false,
            onStopSelected = onStopSelected,
            onMapClick = onMapClick,
            contentPadding = contentPadding,
            modifier = Modifier.zIndex(0f)
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun BoxScope.LocationButton(
    locationPermissionState: PermissionState?,
    cameraPositionState: CameraPositionState,
    locationService: LocationService,
    onReject: suspend () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasPermission = locationPermissionState?.status?.isGranted ?: false
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val snackbarState = LocalSnackbarHostState.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val location by locationService.requestLocationUpdates().collectAsStateWithLifecycle(null, lifecycle)
    val isCameraInCurrentPosition = location?.toLatLng()?.isApproximatelyEqualTo(cameraPositionState.position.target) == true

    val outsideSevillaMessenger = remember { OutsideSevillaMessenger() }

    CustomFab(
        onClick = {
            if (!hasPermission) {
                locationPermissionState?.launchPermissionRequest()
            } else {
                scope.launch {
                    locationService.obtainCurrentLocation()?.toLatLng()?.let { userLocation ->
                        if (userLocation.isInsideSevilla()) {
                            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOCATION_ZOOM))
                        } else {
                            view.performHapticReject()
                            launch {
                                snackbarState.currentSnackbarData?.dismiss()
                                snackbarState.showSnackbar(outsideSevillaMessenger.getNextMessage())
                            }
                            launch { onReject() }
                        }
                    }
                }
            }
        },
        color = SevTheme.colorScheme.background,
        contentColor = if (isCameraInCurrentPosition) SevTheme.colorScheme.primary else SevTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp),
    ) {
        Icon(if (isCameraInCurrentPosition) Icons.Filled.MyLocation else Icons.Filled.LocationSearching, "Search Location")
    }

}

@Composable
private fun DebugInfo(
    state: MapScreenState,
    cameraPositionState: CameraPositionState,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
    ) {
        Text(state::class.simpleName!!)
        val zoom = cameraPositionState.position.zoom
        Text(format("%.2f", zoom) + " - " + ZoomLevel(zoom.toInt()))
    }
}

const val MY_LOCATION_ZOOM = 17f


@OptIn(ExperimentalPermissionsApi::class)
@Preview
@Composable
private fun MapScreenPreview() {
    ScreenPreview {
        MapUI(
            state = MapScreenState.Initial,
            contentPadding = PaddingValues(),
            cameraPositionState = CameraPositionState(),
            locationPermissionState = null,
            locationService = NoopLocationService,
            onStopSelected = {},
            onMapClick = {},
        )
    }
}
