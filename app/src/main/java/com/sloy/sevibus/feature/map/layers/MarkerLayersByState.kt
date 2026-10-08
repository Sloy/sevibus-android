package com.sloy.sevibus.feature.map.layers

import androidx.compose.runtime.Composable
import com.google.maps.android.compose.GoogleMapComposable
import com.sloy.sevibus.domain.model.Stop
import com.sloy.sevibus.feature.debug.MapDebugOptions
import com.sloy.sevibus.feature.map.MapScreenState
import com.sloy.sevibus.feature.map.ZoomLevel

@Composable
@GoogleMapComposable
fun MarkerLayersByState(
    state: MapScreenState,
    zoomLevel: ZoomLevel,
    onStopClick: (Stop) -> Unit,
    showBuses: Boolean = true,
    debugOptions: MapDebugOptions = MapDebugOptions(),
) {
    val filtered = state.withoutHiddenMarkers(showBuses, debugOptions)
    GenericStopsMakerLayer(
        stops = filtered.genericStops(),
        zoomLevel = zoomLevel,
        onStopClick = onStopClick,
        hideOnZoom = filtered.genericStopsHiddenZooms,
        colored = !filtered.hasLineSelected,
    )
    when (filtered) {
        is MapScreenState.Initial, is MapScreenState.Idle -> {}
        is MapScreenState.LinesOverview -> LinesOverviewMarkerLayers(filtered, zoomLevel, onStopClick)
        is MapScreenState.LineSelected -> LineSelectedMarkerLayers(filtered, zoomLevel, onStopClick)
        is MapScreenState.StopSelected -> StopSelectedMarkerLayers(filtered, zoomLevel, onStopClick)
        is MapScreenState.StopAndLineSelected -> StopAndLineSelectedMarkerLayers(filtered, zoomLevel, onStopClick)
    }
}

private fun MapScreenState.withoutHiddenMarkers(showBuses: Boolean, options: MapDebugOptions): MapScreenState {
    val withoutStops = if (options.hideStops) withoutStops() else this
    return if (showBuses && !options.hideBuses) withoutStops else withoutStops.withoutBuses()
}

private fun MapScreenState.withoutStops(): MapScreenState = when (this) {
    is MapScreenState.Initial -> this
    is MapScreenState.Idle -> copy(allStops = emptyList())
    is MapScreenState.LinesOverview -> copy(allStops = emptyList())
    is MapScreenState.LineSelected -> copy(otherStops = emptyList())
    is MapScreenState.StopSelected -> copy(otherStops = emptyList())
    is MapScreenState.StopAndLineSelected -> copy(lineSelectedState = lineSelectedState.copy(otherStops = emptyList()))
}

private fun MapScreenState.genericStops(): List<Stop> = when (this) {
    is MapScreenState.Initial -> emptyList()
    is MapScreenState.Idle -> allStops
    is MapScreenState.LinesOverview -> allStops
    is MapScreenState.LineSelected -> otherStops
    is MapScreenState.StopSelected -> otherStops
    is MapScreenState.StopAndLineSelected -> lineSelectedState.otherStops
}

private val ZoomedOutLevels = listOf(ZoomLevel.Far, ZoomLevel.Medium)

private val MapScreenState.genericStopsHiddenZooms: List<ZoomLevel>
    get() = if (this is MapScreenState.Idle || this is MapScreenState.StopSelected) emptyList() else ZoomedOutLevels

private val MapScreenState.hasLineSelected: Boolean
    get() = this is MapScreenState.LineSelected || this is MapScreenState.StopAndLineSelected

private fun MapScreenState.withoutBuses(): MapScreenState = when (this) {
    is MapScreenState.LineSelected -> copy(buses = null)
    is MapScreenState.StopSelected -> copy(buses = null)
    is MapScreenState.StopAndLineSelected -> copy(lineSelectedState = lineSelectedState.copy(buses = null))
    else -> this
}

@Composable
@GoogleMapComposable
private fun LinesOverviewMarkerLayers(state: MapScreenState.LinesOverview, zoomLevel: ZoomLevel, onStopClick: (Stop) -> Unit) {
    if (state.linePaths != null) {
        MultipleLinesLayer(state.linePaths, zoomLevel)
    }
}

@Composable
@GoogleMapComposable
private fun LineSelectedMarkerLayers(state: MapScreenState.LineSelected, zoomLevel: ZoomLevel, onStopClick: (Stop) -> Unit) {
    LineStopsMarkerLayer(state.lineStops, state.line, zoomLevel, onStopClick)
    if (state.path != null) SingleLineLayer(state.path, zoomLevel)
    if (state.buses != null) BusMarkersLayer(state.buses, showLineTooltip = false)
}

@Composable
@GoogleMapComposable
private fun StopSelectedMarkerLayers(state: MapScreenState.StopSelected, zoomLevel: ZoomLevel, onStopClick: (Stop) -> Unit) {
    SelectedStopLayer(state.selectedStop, zoomLevel, onStopClick)
    if (state.linesPaths != null) MultipleLinesLayer(state.linesPaths, zoomLevel, splitPoint = state.selectedStop.position)
    if (state.buses != null) BusMarkersLayer(state.buses, showLineTooltip = true)
}

@Composable
@GoogleMapComposable
private fun StopAndLineSelectedMarkerLayers(state: MapScreenState.StopAndLineSelected, zoomLevel: ZoomLevel, onStopClick: (Stop) -> Unit) {
    SelectedStopLayer(state.selectedStop, zoomLevel, onStopClick, color = state.lineSelectedState.line.color)
    LineStopsMarkerLayer(state.lineSelectedState.lineStops - state.selectedStop, state.lineSelectedState.line, zoomLevel, onStopClick)
    if (state.lineSelectedState.path != null) SingleLineLayer(state.lineSelectedState.path, zoomLevel, splitPoint = state.selectedStop.position)
    if (state.lineSelectedState.buses != null) BusMarkersLayer(state.lineSelectedState.buses, showLineTooltip = false)
}

