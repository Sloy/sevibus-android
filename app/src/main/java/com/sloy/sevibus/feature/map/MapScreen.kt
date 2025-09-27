package com.sloy.sevibus.feature.map

import android.Manifest
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableSharedFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.core.BottomSheetState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.sloy.debugmenu.launcher.DebugMenuLauncher
import com.google.android.gms.maps.model.LatLng
import com.sloy.sevibus.domain.model.Stop
import com.sloy.sevibus.domain.model.isInsideSevilla
import com.sloy.sevibus.domain.model.toLatLng
import com.sloy.sevibus.feature.debug.SevDebugMenu
import com.sloy.sevibus.infrastructure.BuildVariant
import com.sloy.sevibus.infrastructure.EventCollector
import com.sloy.sevibus.infrastructure.FeatureFlags
import com.sloy.sevibus.infrastructure.analytics.SevEvent
import com.sloy.sevibus.infrastructure.analytics.events.Clicks
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
import org.koin.androidx.compose.koinViewModel


@Composable
fun MapScreen(
    sheetState: BottomSheetState,
    contentPadding: PaddingValues,
    onStopSelected: (stop: Stop) -> Unit,
    onMapClick: () -> Unit,
) {
    if (!LocalView.current.isInEditMode) {
        val mapViewModel: MapViewModel = koinViewModel()
        val state by mapViewModel.state.collectAsStateWithLifecycle()
        val snackbarState = LocalSnackbarHostState.current
        MapScreen(sheetState, state, contentPadding, onStopSelected, onMapClick, mapViewModel::onTrack, snackbarState)
        EventCollector(mapViewModel.events) { event ->
            when (event) {
                is MapScreenEvent.Error -> snackbarState.showSnackbar(event.message, withDismissAction = true)
            }
        }
    } else {
        MapScreen(sheetState, MapScreenState.Initial, contentPadding, onStopSelected, onMapClick, { }, SnackbarHostState())
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun MapScreen(
    sheetState: BottomSheetState,
    state: MapScreenState,
    contentPadding: PaddingValues,
    onStopSelected: (stop: Stop) -> Unit,
    onMapClick: () -> Unit,
    onTrack: (SevEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val locationPermissionState = rememberPermissionStateOnUI(Manifest.permission.ACCESS_FINE_LOCATION)

    MapUI(
        sheetState,
        state,
        locationPermissionState,
        snackbarHostState,
        contentPadding,
        onStopSelected,
        onMapClick,
        onTrack,
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun MapUI(
    sheetState: BottomSheetState,
    state: MapScreenState,
    locationPermissionState: PermissionState?,
    snackbarHostState: SnackbarHostState,
    contentPadding: PaddingValues,
    onStopSelected: (stop: Stop) -> Unit,
    onMapClick: () -> Unit,
    onTrack: (SevEvent) -> Unit,
) {
    val locationService: LocationService = koinInjectOnUI() ?: NoopLocationService
    val shakeAnim = remember { Animatable(0f) }
    val locationButtonClickFlow = remember { MutableSharedFlow<Unit>(extraBufferCapacity = 1) }
    var currentCameraPosition by remember { mutableStateOf<LatLng?>(null) }

    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer { translationX = shakeAnim.value }) {
        if (FeatureFlags.showMapDebugInfo) {
            DebugInfo(
                state,
                Modifier
                    .zIndex(1f)
                    .padding(contentPadding)
            )
        }
        Column(
            Modifier
                .padding(contentPadding)
                .align(Alignment.BottomEnd)
                .zIndex(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DebugButton()
            LocationButton(
                locationPermissionState,
                snackbarHostState,
                locationService,
                currentCameraPosition,
                onReject = {
                    shakeAnim.animateShake()
                },
                onTrack = onTrack,
                onLocationButtonClicked = {
                    locationButtonClickFlow.tryEmit(Unit)
                }
            )
        }

        SevMap(
            state = state,
            hasLocationPermission = locationPermissionState?.status?.isGranted ?: false,
            onStopSelected = onStopSelected,
            onMapClick = onMapClick,
            contentPadding = contentPadding,
            sheetState = sheetState,
            locationButtonClickFlow = locationButtonClickFlow,
            onCameraPositionChanged = { position ->
                currentCameraPosition = position
            },
            modifier = Modifier.zIndex(0f)
        )
    }
}

@Composable
private fun DebugButton(modifier: Modifier = Modifier) {
    if (BuildVariant.isRelease()) return
    val context = LocalContext.current
    CustomFab(
        onClick = {
            DebugMenuLauncher.launchMenu(context) { SevDebugMenu() }
        },
        color = SevTheme.colorScheme.background,
        contentColor = SevTheme.colorScheme.onSurfaceVariant,
        size = 38.dp,
        modifier = modifier
    ) {
        Icon(Icons.Filled.BugReport, "Debug", Modifier.size(16.dp))
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun LocationButton(
    locationPermissionState: PermissionState?,
    snackbarState: SnackbarHostState,
    locationService: LocationService,
    currentCameraPosition: LatLng?,
    onReject: suspend () -> Unit,
    onTrack: (SevEvent) -> Unit,
    onLocationButtonClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasPermission = locationPermissionState?.status?.isGranted ?: false
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val location by locationService.requestLocationUpdates().collectAsStateWithLifecycle(null, lifecycle)

    val isCameraInCurrentPosition = currentCameraPosition?.let { cameraPos ->
        location?.toLatLng()?.isApproximatelyEqualTo(cameraPos) == true
    } ?: false

    val outsideSevillaMessenger = remember { OutsideSevillaMessenger() }

    CustomFab(
        onClick = {
            if (!hasPermission) {
                locationPermissionState?.launchPermissionRequest()
                onTrack(Clicks.LocationButtonClicked("no-permission"))
            } else {
                scope.launch {
                    locationService.obtainCurrentLocation()?.toLatLng()?.let { userLocation ->
                        if (userLocation.isInsideSevilla()) {
                            onTrack(Clicks.LocationButtonClicked("inside-sevilla"))
                            onLocationButtonClicked()
                        } else {
                            onTrack(Clicks.LocationButtonClicked("outside-sevilla"))
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
            .padding(16.dp),
    ) {
        Icon(if (isCameraInCurrentPosition) Icons.Filled.MyLocation else Icons.Filled.LocationSearching, "Search Location")
    }
}

@Composable
private fun DebugInfo(
    state: MapScreenState,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
    ) {
        Text(state::class.simpleName!!)
        Text("Debug Info")
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Preview
@Composable
private fun MapScreenPreview() {
    ScreenPreview {
        val scope = rememberCoroutineScope()
        val density = androidx.compose.ui.platform.LocalDensity.current
        MapScreen(
            sheetState = remember {
                com.composables.core.BottomSheetState(
                    initialDetent = com.composables.core.SheetDetent.Hidden,
                    detents = listOf(com.composables.core.SheetDetent.Hidden),
                    coroutineScope = scope,
                    animationSpec = androidx.compose.animation.core.spring(),
                    velocityThreshold = { 125.dp.value },
                    positionalThreshold = { 56.dp.value },
                    decayAnimationSpec = androidx.compose.animation.core.exponentialDecay(),
                    confirmDetentChange = { true },
                    density = { density }
                )
            },
            contentPadding = PaddingValues(),
            onStopSelected = {},
            onMapClick = {}
        )
    }
}
