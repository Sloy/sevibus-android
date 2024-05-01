package com.sloydev.sevibus.feature.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import com.sloydev.sevibus.domain.model.Line
import com.sloydev.sevibus.domain.model.Route
import com.sloydev.sevibus.domain.model.Stop
import com.sloydev.sevibus.navigation.TopLevelDestination
import com.sloydev.sevibus.ui.preview.ScreenPreview
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.mapRoute() {
    composable(TopLevelDestination.MAP.route) {
        MapScreen()
    }
}

@Composable
fun MapScreen() {
    val viewModel: MapViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    MapScreen(
        state,
        onStopSelected = viewModel::onStopSelected,
        onLineSelected = { viewModel.onLineSelected(it) },
        onRouteSelected = { viewModel.onRouteSelected(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    state: MapScreenState,
    onStopSelected: (Stop?) -> Unit = {},
    onLineSelected: (Line?) -> Unit = {},
    onRouteSelected: (Route) -> Unit = {},
) {


    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val scope = rememberCoroutineScope()
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        skipHiddenState = false
    )

    LaunchedEffect(state::class) {
        when (state) {
            is MapScreenState.Initial, is MapScreenState.Idle -> sheetState.hide()
            else -> sheetState.partialExpand()
        }
    }

    BottomSheetScaffold(
        scaffoldState = rememberBottomSheetScaffoldState(sheetState),
        sheetPeekHeight = screenHeight / 3,
        sheetContent = {
            BottomSheetContent(state)
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            MapContent(state, innerPadding.takeIf { sheetState.isVisible }, onStopSelected = {
                onStopSelected(it)
            })
        }
    }
}

@Composable
fun BottomSheetContent(state: MapScreenState) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Text(text = "This is the Bottom sheet for $state")
    }
}

@Composable
fun MapContent(
    state: MapScreenState,
    contentPadding: PaddingValues?,
    onStopSelected: (stop: Stop?) -> Unit,
) {
    val triana = LatLng(37.385222, -6.011210)
    val recaredo = LatLng(37.389083, -5.984483)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(triana, 15.5f)
    }
    /*if (stops.isNotEmpty()) {
        LaunchedEffect(stops) {
            val bounds = LatLngBounds.Builder().apply {
                stops.forEach { include(it.position.toLatLng()) }
            }.build()
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 32)
            cameraPositionState.animate(cameraUpdate, 200)
        }
    }*/

    DebugInfo(state, cameraPositionState)

    SevMap(
        state = state,
        cameraPositionState = cameraPositionState,
        onStopSelected = onStopSelected,
        onMapClick = { onStopSelected(null) },
        contentPadding = contentPadding,
    )
}

@Composable
private fun DebugInfo(
    state: MapScreenState,
    cameraPositionState: CameraPositionState
) {
    Column(
        verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f)
    ) {
        Text(state::class.simpleName!!)
        Text(cameraPositionState.position.zoom.toString())
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
