package com.sloydev.sevibus.feature.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.sloydev.sevibus.domain.model.SearchResult
import com.sloydev.sevibus.domain.model.Stop
import com.sloydev.sevibus.feature.search.SearchWidget
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

@Composable
fun MapScreen(
    state: MapScreenState,
    onStopSelected: (Stop?) -> Unit = {},
    onLineSelected: (Line?) -> Unit = {},
    onRouteSelected: (Route) -> Unit = {},
) {
    Box(Modifier.fillMaxSize()) {
        Map(
            state,
            onStopSelected = onStopSelected,
            onStopDismissed = { onStopSelected(null) },
        )
        Column {
            SearchWidget(
                onSearchResultClicked = {
                    if (it is SearchResult.LineResult) {
                        onLineSelected(it.line)
                    }
                },
                Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
            )
        }
    }
}

@Composable
fun Map(
    state: MapScreenState,
    onStopSelected: (stop: Stop) -> Unit,
    onStopDismissed: () -> Unit,
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
        onMapClick = onStopDismissed
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
