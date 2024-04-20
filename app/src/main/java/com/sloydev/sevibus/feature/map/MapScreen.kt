package com.sloydev.sevibus.feature.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.google.android.gms.maps.model.AdvancedMarkerOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.feature.lines.SearchResult
import com.sloydev.sevibus.feature.lines.SevSearchBar
import com.sloydev.sevibus.feature.linestops.Stop
import com.sloydev.sevibus.feature.stopdetail.StopDetailScreen
import com.sloydev.sevibus.navigation.TopLevelDestination
import com.sloydev.sevibus.ui.ScreenPreview
import com.sloydev.sevibus.ui.components.LineIndicatorSmall

fun NavGraphBuilder.mapRoute() {
    composable(TopLevelDestination.MAP.route) {
        MapScreen(Stubs.searchResults.take(3))
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MapScreen(previewFilters: List<SearchResult> = emptyList()) {
    Box(Modifier.fillMaxSize()) {
        var showBottomSheet by remember { mutableStateOf(false) }
        val filters = remember { mutableStateListOf<SearchResult>(*previewFilters.toTypedArray()) }
        Map(
            Modifier.fillMaxSize(),
            onStopClick = { showBottomSheet = true },
            onStopDismissed = { showBottomSheet = false },
        )
        Column {
            SevSearchBar(
                onSearchResultClicked = { filters.add(it) },
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
                filters.forEach { filter ->
                    MapFilterChip(filter, onRemoveFilter = { assert(filters.remove(it)) }, modifier = Modifier.padding(end = 8.dp))
                }

            }
        }

        if (showBottomSheet) {
            val state = rememberBottomSheetScaffoldState()
            BottomSheetScaffold(
                sheetPeekHeight = (104).dp,
                scaffoldState = state,
                sheetContent = { StopDetailScreen(Stubs.stops[0], embedded = true) },
                content = {}
            )
            LaunchedEffect(showBottomSheet) {
                state.bottomSheetState.expand()
            }
        }
    }
}

@Composable
fun MapFilterChip(filter: SearchResult, onRemoveFilter: (SearchResult) -> Unit, modifier: Modifier = Modifier) {
    //var selected by remember { mutableStateOf(true) }
    val label: @Composable () -> Unit = when (filter) {
        is SearchResult.LineResult -> {
            { LineIndicatorSmall(filter.line) }
        }

        is SearchResult.StopResult -> {
            { Text(filter.stop.code.toString()) }
        }
    }
    ElevatedAssistChip(
        modifier = modifier,
//        selected = selected,
        onClick = { /*selected = !selected*/ },
        label = { label() },
        leadingIcon = {
            /*if (selected) {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }*/
        },
        trailingIcon = {
            IconButton(
                modifier = Modifier.size(FilterChipDefaults.IconSize),
                onClick = { onRemoveFilter(filter) }) {
                Icon(Icons.Default.Close, contentDescription = "Remove")
            }
        }
    )
}

@Composable
fun Map(modifier: Modifier, onStopClick: (code: Int) -> Unit, onStopDismissed: () -> Unit) {
    val triana = LatLng(37.385222, -6.011210)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(triana, 15.5f)
    }

    var mapProperties by remember {
        mutableStateOf(
            MapProperties(
                minZoomPreference = 12f,
                //isMyLocationEnabled = true,
                isTrafficEnabled = true
            )
        )
    }
    var mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                mapToolbarEnabled = true,
                compassEnabled = true
            )
        )
    }

    val stops = remember { Stubs.stops }
    GoogleMap(
        modifier = modifier,
        uiSettings = mapUiSettings,
        properties = mapProperties,
        cameraPositionState = cameraPositionState,
        onMapClick = { onStopDismissed() }
    ) {
        stops.forEach { stop ->
            AdvancedMarker(
                state = MarkerState(position = stop.position.toLatLng()),
                title = stop.code.toString(),
                snippet = stop.code.toString(),
                flat = true,
                onClick = {
                    onStopClick(stop.code)
                    false
                },
                collisionBehavior = AdvancedMarkerOptions.CollisionBehavior.OPTIONAL_AND_HIDES_LOWER_PRIORITY


            )
        }
    }
}

private fun Stop.Position.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

@Preview
@Composable
private fun MapScreenPreview() {
    ScreenPreview {
        MapScreen(previewFilters = Stubs.searchResults.take(3))
    }
}