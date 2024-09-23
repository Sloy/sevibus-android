package com.sloy.sevibus.feature.map

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.Stop
import com.sloy.sevibus.domain.model.toLatLng
import com.sloy.sevibus.infrastructure.extensions.koinInjectOnUI
import com.sloy.sevibus.infrastructure.location.LocationService
import com.sloy.sevibus.infrastructure.location.NoopLocationService
import com.sloy.sevibus.ui.preview.ScreenPreview
import com.sloy.sevibus.ui.theme.SevTheme
import kotlinx.coroutines.launch
import okhttp3.internal.format


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapContent(
    state: MapScreenState,
    contentPadding: PaddingValues,
    onStopSelected: (stop: Stop) -> Unit,
    onMapClick: () -> Unit,
) {
    val locationService: LocationService = koinInjectOnUI() ?: NoopLocationService
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(Stubs.locationRecaredo.toLatLng(), 15.5f)
    }
    LaunchedEffect(locationPermissionState.status.isGranted) {
        locationService.obtainCurrentLocation()?.toLatLng()?.let {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 18f))
        }
    }
    LaunchedEffect(cameraPositionState.cameraMoveStartedReason) {
        if (cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE) {
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
    Box(Modifier.fillMaxSize()) {
        DebugInfo(state, cameraPositionState, Modifier.zIndex(1f))
        LocationButton(
            locationPermissionState, cameraPositionState, locationService,
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
    modifier: Modifier = Modifier
) {
    val hasPermission = locationPermissionState?.status?.isGranted ?: false
    val scope = rememberCoroutineScope()
    FloatingActionButton(
        onClick = {
            if (!hasPermission) {
                locationPermissionState?.launchPermissionRequest()
            } else {
                scope.launch {
                    locationService.obtainCurrentLocation()?.toLatLng()?.let {
                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 18f))
                    }
                }
            }
        },
        containerColor = SevTheme.colorScheme.primary,
        contentColor = SevTheme.colorScheme.onPrimary,
        modifier = modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp),
    ) {
        Icon(Icons.Filled.MyLocation, "Floating action button.")
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
        Text(format("%.2f", cameraPositionState.position.zoom))
    }
}


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
