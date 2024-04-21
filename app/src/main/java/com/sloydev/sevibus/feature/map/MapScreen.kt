package com.sloydev.sevibus.feature.map

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.sloydev.sevibus.R
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.domain.SearchResult
import com.sloydev.sevibus.feature.lines.SevSearchBar
import com.sloydev.sevibus.domain.plus
import com.sloydev.sevibus.domain.toLatLng
import com.sloydev.sevibus.feature.stopdetail.StopDetailScreen
import com.sloydev.sevibus.navigation.TopLevelDestination
import com.sloydev.sevibus.ui.preview.ScreenPreview
import com.sloydev.sevibus.ui.components.LineIndicatorSmall
import com.sloydev.sevibus.ui.icons.SevIcons

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
                //state.bottomSheetState.expand()
            }
        }
        BackHandler(showBottomSheet) {
            showBottomSheet = false
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
    ElevatedAssistChip(
        modifier = modifier,
        onClick = { /* TODO */ },
        label = { label() },
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
fun BoxScope.Map(modifier: Modifier, onStopClick: (code: Int) -> Unit, onStopDismissed: () -> Unit) {
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
                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_retro)
            )
        )
    }
    var mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                mapToolbarEnabled = false,
                compassEnabled = true
            )
        )
    }

    val icon = SevIcons.MapIcon.stop.getRes(cameraPositionState.position.zoom)
    val stops = remember { Stubs.stops + Stubs.stops.map { it.copy(position = it.position + (0.0001 to 0.0001)) } }
    var selectedMarker by remember { mutableStateOf<LatLng?>(null) }

    GoogleMap(
        modifier = modifier,
        uiSettings = mapUiSettings,
        properties = mapProperties,
        cameraPositionState = cameraPositionState,
        onMapClick = {
            onStopDismissed()
            selectedMarker = null
        }
    ) {
        val iconFactory = remember(icon) { BitmapDescriptorFactory.fromResource(icon) }
        stops.forEach { stop ->
            Marker(
                state = MarkerState(position = stop.position.toLatLng()),
                title = stop.code.toString(),
                anchor = Offset(0.5f, 0.5f),
                onClick = {
                    onStopClick(stop.code)
                    selectedMarker = it.position
                    true
                },
                icon = iconFactory,
            )
        }

        selectedMarker?.let {
            Marker(
                state = MarkerState(position = it),
                onClick = { true },
                zIndex = 100f
            )
        }
    }
}

@Preview
@Composable
private fun MapScreenPreview() {
    ScreenPreview {
        MapScreen(previewFilters = Stubs.searchResults.take(3))
    }
}