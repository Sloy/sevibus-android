package com.sloy.sevibus.feature.map.layers

import androidx.compose.runtime.Composable
import com.google.maps.android.compose.GoogleMapComposable
import com.sloy.sevibus.domain.model.Stop
import com.sloy.sevibus.feature.map.MapScreenState
import com.sloy.sevibus.feature.map.ZoomLevel

@Composable
@GoogleMapComposable
fun MarkerLayersByState(state: MapScreenState, zoomLevel: ZoomLevel, onStopClick: (Stop) -> Unit) {
    when (state) {
        is MapScreenState.Initial -> {}
        is MapScreenState.Idle -> IdleMarkerLayers(state, zoomLevel, onStopClick)
        is MapScreenState.LinesOverview -> LinesOverviewMarkerLayers(state, zoomLevel, onStopClick)
        is MapScreenState.LineSelected -> LineSelectedMarkerLayers(state, zoomLevel, onStopClick)
        is MapScreenState.StopSelected -> StopSelectedMarkerLayers(state, zoomLevel, onStopClick)
        is MapScreenState.StopAndLineSelected -> StopAndLineSelectedMarkerLayers(state, zoomLevel, onStopClick)
    }
}

@Composable
@GoogleMapComposable
private fun IdleMarkerLayers(state: MapScreenState.Idle, zoomLevel: ZoomLevel, onStopClick: (Stop) -> Unit) {
    GenericStopsMakerLayer(state.allStops, zoomLevel, onStopClick, colored = true)
}

@Composable
@GoogleMapComposable
private fun LinesOverviewMarkerLayers(state: MapScreenState.LinesOverview, zoomLevel: ZoomLevel, onStopClick: (Stop) -> Unit) {
    GenericStopsMakerLayer(state.allStops, zoomLevel, onStopClick, colored = true, hideOnZoom = listOf(ZoomLevel.Far, ZoomLevel.Medium))
    if (state.linePaths != null) {
        MultipleLinesLayer(state.linePaths, zoomLevel)
    }
}

@Composable
@GoogleMapComposable
private fun LineSelectedMarkerLayers(state: MapScreenState.LineSelected, zoomLevel: ZoomLevel, onStopClick: (Stop) -> Unit) {
    LineStopsMarkerLayer(state.lineStops, state.line, zoomLevel, onStopClick)
    GenericStopsMakerLayer(state.otherStops, zoomLevel, onStopClick, colored = false, hideOnZoom = listOf(ZoomLevel.Far, ZoomLevel.Medium))
    if (state.path != null) SingleLineLayer(state.path, zoomLevel)
    if (state.buses != null) BusMarkersLayer(state.buses, showLineTooltip = false)
}

@Composable
@GoogleMapComposable
private fun StopSelectedMarkerLayers(state: MapScreenState.StopSelected, zoomLevel: ZoomLevel, onStopClick: (Stop) -> Unit) {
    SelectedStopLayer(state.selectedStop, zoomLevel, onStopClick)
    GenericStopsMakerLayer(state.otherStops, zoomLevel, onStopClick, colored = true)
    if (state.linesPaths != null) MultipleLinesLayer(state.linesPaths, zoomLevel, splitPoint = state.selectedStop.position)
    if (state.buses != null) BusMarkersLayer(state.buses, showLineTooltip = true)
}

@Composable
@GoogleMapComposable
private fun StopAndLineSelectedMarkerLayers(state: MapScreenState.StopAndLineSelected, zoomLevel: ZoomLevel, onStopClick: (Stop) -> Unit) {
    SelectedStopLayer(state.selectedStop, zoomLevel, onStopClick, color = state.lineSelectedState.line.color)
    LineStopsMarkerLayer(state.lineSelectedState.lineStops - state.selectedStop, state.lineSelectedState.line, zoomLevel, onStopClick)
    GenericStopsMakerLayer(state.lineSelectedState.otherStops, zoomLevel, onStopClick, colored = false, hideOnZoom = listOf(ZoomLevel.Far, ZoomLevel.Medium))
    if (state.lineSelectedState.path != null) SingleLineLayer(state.lineSelectedState.path, zoomLevel, splitPoint = state.selectedStop.position)
    if (state.lineSelectedState.buses != null) BusMarkersLayer(state.lineSelectedState.buses, showLineTooltip = false)
}

