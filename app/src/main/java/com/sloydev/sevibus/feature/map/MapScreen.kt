package com.sloydev.sevibus.feature.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.domain.model.Line
import com.sloydev.sevibus.domain.model.Route
import com.sloydev.sevibus.domain.model.RoutePath
import com.sloydev.sevibus.domain.model.SearchResult
import com.sloydev.sevibus.domain.model.Stop
import com.sloydev.sevibus.domain.model.toLatLng
import com.sloydev.sevibus.domain.model.toUiColor
import com.sloydev.sevibus.feature.linestops.LineRouteScreen
import com.sloydev.sevibus.feature.linestops.LineRouteScreenState
import com.sloydev.sevibus.feature.search.SearchWidget
import com.sloydev.sevibus.navigation.TopLevelDestination
import com.sloydev.sevibus.ui.components.LineIndicatorSmall
import com.sloydev.sevibus.ui.icons.SevIcons
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
    val line by viewModel.selectedLine.collectAsState()
    val route by viewModel.selectedRoute.collectAsState()
    val stops by viewModel.stops.collectAsState(initial = emptyList())
    val path by viewModel.path.collectAsState(initial = null)

    MapScreen(
        line,
        route,
        stops,
        path,
        onLineSelected = { viewModel.onLineSelected(it) },
        onRouteSelected = { viewModel.onRouteSelected(it) })
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MapScreen(
    selectedLine: Line?,
    selectedRoute: Route?,
    stops: List<Stop>,
    path: RoutePath?,
    onLineSelected: (Line?) -> Unit,
    onRouteSelected: (Route) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        Map(
            Modifier.fillMaxSize(),
            selectedLine,
            stops,
            path,
            onStopClick = { },
            onStopDismissed = {
                //TODO actually working?
                //onLineSelected(null)
            },
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
            FlowRow(
                Modifier
                    .fillMaxWidth(1f)
                    .padding(horizontal = 16.dp)
                    .wrapContentHeight(align = Alignment.Top),
                horizontalArrangement = Arrangement.Start,
            ) {
                if (selectedLine != null) {
                    MapFilterChip(SearchResult.LineResult(selectedLine), onRemoveFilter = { }, modifier = Modifier.padding(end = 8.dp))
                }
            }
        }

        val sheetState = rememberModalBottomSheetState()
        if (false && selectedLine != null && selectedRoute != null && stops.isNotEmpty()) {
            ModalBottomSheet(
                onDismissRequest = {
                    //onLineSelected(null)
                },
                sheetState = sheetState
            ) {
                val state = LineRouteScreenState.Content.Full(selectedLine, selectedRoute, stops)
                LineRouteScreen(state, onRouteSelected = onRouteSelected, onStopClick = { stop -> })
            }
        }
    }
}

@Composable
fun MapFilterChip(filter: SearchResult, onRemoveFilter: (SearchResult) -> Unit, modifier: Modifier = Modifier) {
    val label: @Composable () -> Unit = when (filter) {
        is SearchResult.LineResult -> {
            { LineIndicatorSmall(filter.line) }
        }

        is SearchResult.StopResult -> {
            { Text(filter.stop.code.toString()) }
        }
    }
    ElevatedAssistChip(modifier = modifier, onClick = { /* TODO */ }, label = { label() }, trailingIcon = {
        IconButton(modifier = Modifier.size(FilterChipDefaults.IconSize), onClick = { onRemoveFilter(filter) }) {
            Icon(Icons.Default.Close, contentDescription = "Remove")
        }
    })
}

@Composable
fun BoxScope.Map(
    modifier: Modifier,
    selectedLine: Line?,
    stops: List<Stop>,
    path: RoutePath?,
    onStopClick: (code: Int) -> Unit,
    onStopDismissed: () -> Unit
) {
    val triana = LatLng(37.385222, -6.011210)
    val recaredo = LatLng(37.389083, -5.984483)
    var cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(recaredo, 15.5f)
    }

    Text(
        cameraPositionState.position.zoom.toString(), modifier = Modifier
            .align(Alignment.BottomCenter)
            .zIndex(1f)
    )
    val context = LocalContext.current
    var mapProperties by remember {
        mutableStateOf(
            MapProperties(
                minZoomPreference = 12f,
                //isMyLocationEnabled = true,
                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, com.sloydev.sevibus.R.raw.map_style_default)
            )
        )
    }
    var mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                mapToolbarEnabled = false, compassEnabled = true
            )
        )
    }

    var selectedMarker by remember { mutableStateOf<LatLng?>(null) }

    GoogleMap(modifier = modifier,
        uiSettings = mapUiSettings,
        properties = mapProperties,
        cameraPositionState = cameraPositionState,
        onMapClick = {
            onStopDismissed()
            selectedMarker = null
        }) {
        with(LocalDensity.current) {
            if (cameraPositionState.position.zoom > 18) {
                val icon = SevIcons.MapIcon.stop.getRes(cameraPositionState.position.zoom)
                val iconFactory = remember(icon) { BitmapDescriptorFactory.fromResource(icon) }
                stops.forEach { stop ->
                    Marker(
                        state = MarkerState(position = stop.position.toLatLng()),
                        anchor = Offset(0.5f, 0.5f),
                        onClick = {
                            onStopClick(stop.code)
                            selectedMarker = it.position
                            false
                        },
                        icon = iconFactory,
                    )
                }
            }

            selectedMarker?.let {
                Marker(
                    state = MarkerState(position = it), onClick = { true }, zIndex = 100f
                )
            }

            if (selectedLine != null && path != null) {
                val circularStopIcon = remember(selectedLine) {
                    BitmapDescriptorFactory.fromBitmap(SevIcons.CircularStopMarker.bitmap(context, selectedLine.color))
                }

                Polyline(
                    points = path.points.map { it.toLatLng() },
                    color = selectedLine.color.toUiColor(),
                    jointType = JointType.ROUND,
                    width = 8.dp.toPx(),
                )

                stops.map { stop -> stop.moveToPath(path) }
                    .forEach { stop ->
                        Marker(
                            state = MarkerState(position = stop.position.toLatLng()),
                            anchor = Offset(0.5f, 0.5f),
                            icon = circularStopIcon,
                        )
                    }
            }
        }
    }
}

private fun Stop.moveToPath(path: RoutePath): Stop {
    val snappedPoint = path.points.minBy { manhattanDistance(this.position, it) }
    return this.copy(position = snappedPoint)
}

private fun manhattanDistance(pos1: Stop.Position, pos2: Stop.Position): Double {
    val latDiff = Math.abs(pos1.latitude - pos2.latitude)
    val lonDiff = Math.abs(pos1.longitude - pos2.longitude)
    return latDiff + lonDiff
}

@Preview
@Composable
private fun MapScreenPreview() {
    ScreenPreview {
        MapScreen(
            selectedLine = Stubs.lines[0],
            selectedRoute = Stubs.lines[0].routes.first(),
            stops = Stubs.stops,
            path = null,
            onLineSelected = {},
            onRouteSelected = {},
        )
    }
}