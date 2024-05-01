package com.sloydev.sevibus.feature.map

import android.graphics.Point
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.Projection
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.sloydev.sevibus.R
import com.sloydev.sevibus.domain.model.PositionBounds
import com.sloydev.sevibus.domain.model.Stop
import com.sloydev.sevibus.domain.model.StopId
import com.sloydev.sevibus.domain.model.filterInBounds
import com.sloydev.sevibus.domain.model.fromLatLng
import com.sloydev.sevibus.domain.model.moveToPath
import com.sloydev.sevibus.domain.model.toLatLng
import com.sloydev.sevibus.domain.model.toUiColor
import com.sloydev.sevibus.infrastructure.SevLogger
import com.sloydev.sevibus.ui.icons.SevIcons
import kotlin.math.abs

@Composable
fun SevMap(
    state: MapScreenState,
    cameraPositionState: CameraPositionState,
    onStopSelected: (Stop) -> Unit,
    onMapClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues? = null,
) {
    val mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                mapToolbarEnabled = false,
                compassEnabled = true,
                myLocationButtonEnabled = true
            )
        )
    }
    val context = LocalContext.current
    val mapProperties by remember {
        mutableStateOf(
            MapProperties(
                minZoomPreference = 13f,
                //isMyLocationEnabled = true,
                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_default)
            )
        )
    }
    val zoomLevel = cameraPositionState.position.zoom.toInt()

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        uiSettings = mapUiSettings,
        properties = mapProperties,
        contentPadding = contentPadding ?: PaddingValues(1.dp),
        cameraPositionState = cameraPositionState,
        onMapClick = { onMapClick() },
    ) {

        SparseStopsMarkers(state, onStopSelected, zoomLevel, cameraPositionState.bounds)
        LineMarkers(state, onStopSelected, zoomLevel)
    }
}

@Composable
@GoogleMapComposable
private fun SparseStopsMarkers(state: MapScreenState, onStopSelected: (Stop) -> Unit, zoomLevel: Int, bounds: PositionBounds?) {
    if (zoomLevel < 15) return
    val visibleStops = when (state) {
        is MapScreenState.Idle -> state.allStops
        is MapScreenState.StopSelected -> state.allStops
        else -> return
    }
    val selectedStop = (state as? MapScreenState.StopSelected)?.selectedStop

    val stopIcon = remember { BitmapDescriptorFactory.fromResource(R.drawable.map_icon_stop_large) }

    visibleStops
        .filterInBounds(bounds)
        .forEach { stop ->
            if (stop != selectedStop) {
                Marker(
                    state = MarkerState(position = stop.position.toLatLng()),
                    anchor = Offset(0.5f, 0.5f),
                    onClick = {
                        onStopSelected(stop)
                        false
                    },
                    icon = stopIcon,
                )
            } else {
                Marker(
                    state = MarkerState(position = stop.position.toLatLng()),
                    onClick = {
                        onStopSelected(stop)
                        false
                    },
                    zIndex = 100f
                )
            }
        }

}

@Composable
@GoogleMapComposable
fun LineMarkers(state: MapScreenState, onStopSelected: (Stop) -> Unit, zoomLevel: Int) {
    val lineSelectedState = when (state) {
        is MapScreenState.LineSelected -> state
        is MapScreenState.LineStopSelected -> state.lineSelectedState
        else -> return
    }
    val selectedStop = (state as? MapScreenState.LineStopSelected)?.selectedStop
    with(LocalDensity.current) {
        val lineWidth = if (zoomLevel < 14) {
            6.dp.toPx()
        } else {
            8.dp.toPx()
        }
        Polyline(
            points = lineSelectedState.path.points.map { it.toLatLng() },
            color = lineSelectedState.line.color.toUiColor(),
            jointType = JointType.ROUND,
            width = lineWidth,
        )
    }

    val context = LocalContext.current
    val circularStopIcon = remember(lineSelectedState.line.color) {
        BitmapDescriptorFactory.fromBitmap(SevIcons.CircularStopMarker.bitmap(context, lineSelectedState.line.color))
    }
    if (zoomLevel > 14) {
        lineSelectedState.lineStops
            .map { stop -> stop.copy(position = stop.position.moveToPath(lineSelectedState.path)) }
            .forEach { stop ->
                if (stop == selectedStop) {
                    Marker(
                        state = MarkerState(position = stop.position.toLatLng()),
                        onClick = {
                            onStopSelected(stop)
                            false
                        },
                        zIndex = 100f
                    )
                }
                Marker(
                    state = MarkerState(position = stop.position.toLatLng()),
                    anchor = Offset(0.5f, 0.5f),
                    icon = circularStopIcon,
                    onClick = {
                        onStopSelected(stop)
                        false
                    }
                )
            }
    }
}

val CameraPositionState.bounds: PositionBounds?
    get() = projection?.visibleRegion?.latLngBounds?.let {
        PositionBounds(
            it.northeast.fromLatLng(),
            it.southwest.fromLatLng()
        )
    }

private fun List<Stop>.withoutCollisions(minDistancePx: Int, projection: Projection?): List<Stop> {
    SevLogger.logD("minDistancePx = ${minDistancePx}px")
    if (projection == null) return emptyList()
    val excludedIds = mutableSetOf<StopId>()
    this.forEachIndexed { i, stop ->
        val point1 = projection.toScreenLocation(stop.position.toLatLng())
        this.drop(i + 1).forEach { otherStop ->
            if (otherStop.code !in excludedIds) {
                val point2 = projection.toScreenLocation(otherStop.position.toLatLng())
                if (manhattanDistance(point1, point2) < minDistancePx) {
                    excludedIds.add(otherStop.code)
                }
            }
        }
    }
    return this.filterNot { it.code in excludedIds }
}


private fun manhattanDistance(pos1: Point, pos2: Point): Int {
    val xDiff = abs(pos1.x - pos2.x)
    val yDiff = abs(pos1.y - pos2.y)
    return (xDiff + yDiff).also {
        SevLogger.logD("manhattanDistance:$it")
    }
}

