package com.sloydev.sevibus.feature.map

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.domain.model.Line
import com.sloydev.sevibus.domain.model.Route
import com.sloydev.sevibus.domain.model.Stop
import com.sloydev.sevibus.domain.model.toLatLng
import com.sloydev.sevibus.feature.linestops.LineRouteScreen
import com.sloydev.sevibus.feature.linestops.LineRouteScreenState
import com.sloydev.sevibus.feature.search.LineSelectorWidget
import com.sloydev.sevibus.feature.stopdetail.StopDetailScreen
import com.sloydev.sevibus.infrastructure.location.LocationService
import com.sloydev.sevibus.navigation.TopLevelDestination
import com.sloydev.sevibus.ui.preview.ScreenPreview
import kotlinx.coroutines.launch
import okhttp3.internal.format
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

fun NavGraphBuilder.mapRoute(setNavigationBarVisibility: (Boolean) -> Unit) {
    composable(TopLevelDestination.MAP.route) {
        MapScreen(setNavigationBarVisibility)
    }
}

@Composable
fun MapScreen(setNavigationBarVisibility: (Boolean) -> Unit) {
    val viewModel: MapViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    viewModel.ticker.collectAsStateWithLifecycle(Unit)

    MapScreen(
        state,
        setNavigationBarVisibility,
        onStopSelected = viewModel::onStopSelected,
        onLineSelected = viewModel::onLineSelected,
        onRouteSelected = viewModel::onRouteSelected,
        onDismiss = viewModel::onDismiss,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    state: MapScreenState,
    onNavigationBarVisibility: (Boolean) -> Unit = {},
    onLineSelected: (Line) -> Unit = {},
    onStopSelected: (Stop) -> Unit = {},
    onRouteSelected: (Route) -> Unit = {},
    onDismiss: () -> Unit = {},
) {


    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        skipHiddenState = false
    )

    onNavigationBarVisibility(sheetState.targetValue == SheetValue.Hidden)

    LaunchedEffect(state::class) {
        when (state) {
            is MapScreenState.Initial, is MapScreenState.Idle -> sheetState.hide()
            else -> sheetState.partialExpand()
        }
    }
    if (sheetState.currentValue == SheetValue.Hidden) {
        LaunchedEffect(sheetState.currentValue) {
            onDismiss()
        }
    }

    BackHandler(enabled = state is Dismissable) {
        onDismiss()
    }

    BottomSheetScaffold(
        scaffoldState = rememberBottomSheetScaffoldState(sheetState),
        sheetPeekHeight = screenHeight / 3,
        sheetContainerColor = MaterialTheme.colorScheme.background,
        sheetContent = {
            BottomSheetContent(state, onStopSelected, onRouteSelected)
        },
    ) { innerPadding ->
        MapContent(state, innerPadding.takeIf { sheetState.isVisible } ?: PaddingValues(), onStopSelected, onLineSelected, onDismiss)
    }
}

@Composable
fun BottomSheetContent(
    state: MapScreenState,
    onStopSelected: (Stop) -> Unit = {},
    onRouteSelected: (Route) -> Unit = {}
) {
    when (state) {
        is MapScreenState.StopSelected -> StopDetailScreen(state.selectedStop.code, embedded = true)
        is MapScreenState.LineStopSelected -> StopDetailScreen(state.selectedStop.code, embedded = true)
        is MapScreenState.LineSelected -> {
            val routeState = if (state.lineStops != null) {
                LineRouteScreenState.Content.Full(state.line, state.selectedRoute, state.lineStops)
            } else {
                LineRouteScreenState.Content.Partial(state.line, state.selectedRoute)
            }
            LineRouteScreen(routeState, onRouteSelected = onRouteSelected, onStopClick = onStopSelected, embedded = true)
        }

        else -> {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(text = "This is the Bottom sheet for $state")
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapContent(
    state: MapScreenState,
    contentPadding: PaddingValues,
    onStopSelected: (stop: Stop) -> Unit,
    onLineSelected: (Line) -> Unit,
    onDismiss: () -> Unit,
) {
    val locationService: LocationService = koinInject()
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(Stubs.locationRecaredo.toLatLng(), 15.5f)
    }
    LaunchedEffect(locationPermissionState.status.isGranted) {
        locationService.obtainCurrentLocation()?.toLatLng()?.let {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 18f))
        }
    }

    Box(Modifier.fillMaxSize()) {
        DebugInfo(state, cameraPositionState, Modifier.zIndex(1f))
        LineSelectorWidget(
            selectedLine = state.selectedLine,
            onLineSelected = { onLineSelected(it) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(1f)
        )
        LocationButton(locationPermissionState, cameraPositionState, locationService,
            Modifier
                .zIndex(1f)
                .padding(contentPadding))
        SevMap(
            state = state,
            cameraPositionState = cameraPositionState,
            locationPermissionState = locationPermissionState,
            onStopSelected = onStopSelected,
            onMapClick = { onDismiss() },
            contentPadding = contentPadding,
            modifier = Modifier.zIndex(0f)
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BoxScope.LocationButton(
    locationPermissionState: PermissionState,
    cameraPositionState: CameraPositionState,
    locationService: LocationService,
    modifier: Modifier = Modifier
) {
    val hasPermission = locationPermissionState.status.isGranted
    val scope = rememberCoroutineScope()
    FloatingActionButton(
        onClick = {
            if (!hasPermission) {
                locationPermissionState.launchPermissionRequest()
            } else {
                scope.launch {
                    locationService.obtainCurrentLocation()?.toLatLng()?.let {
                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 18f))
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
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


@Preview
@Composable
private fun MapScreenPreview() {
    ScreenPreview {
        MapScreen(
            state = MapScreenState.Initial,
        )
    }
}
