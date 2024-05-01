package com.sloydev.sevibus.feature.map

import android.graphics.Point
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.Projection
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.sloydev.sevibus.FakeLocationSource
import com.sloydev.sevibus.R
import com.sloydev.sevibus.domain.model.Path
import com.sloydev.sevibus.domain.model.Position
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SevMap(
    state: MapScreenState,
    cameraPositionState: CameraPositionState,
    locationPermissionState: PermissionState,
    onStopSelected: (Stop) -> Unit,
    onMapClick: () -> Unit,
    contentPadding: PaddingValues?,
    modifier: Modifier = Modifier,
) {
    val mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = false,
                mapToolbarEnabled = false,
                compassEnabled = true,
                myLocationButtonEnabled = true,
            )
        )
    }
    val context = LocalContext.current
    val mapProperties by remember {
        mutableStateOf(
            MapProperties(
                minZoomPreference = 13f,
                isMyLocationEnabled = locationPermissionState.status.isGranted,
                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_default)
            )
        )
    }
    val zoomLevel = cameraPositionState.position.zoom.toInt()

    val selectedStop = when (state) {
        is MapScreenState.LineStopSelected -> state.selectedStop
        is MapScreenState.StopSelected -> state.selectedStop
        else -> null
    }
    if (selectedStop != null) {
        LaunchedEffect(selectedStop) {
            cameraPositionState.zoomInto(selectedStop.position)
        }
    }

    val path = (state as? MapScreenState.LineSelected)?.path
    if (path != null) {
        LaunchedEffect(path) {
            cameraPositionState.centerInPath(path)
        }
    }

    //val locationSource = remember { FakeLocationSource() }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        uiSettings = mapUiSettings,
        properties = mapProperties,
        contentPadding = contentPadding ?: PaddingValues(1.dp),
        cameraPositionState = cameraPositionState,
        onMapClick = { onMapClick() },
        //locationSource = locationSource
    ) {
        SparseStopsMarkers(state, onStopSelected, zoomLevel, cameraPositionState.bounds)
        LineMarkers(state, onStopSelected, zoomLevel)
    }
}

private suspend fun CameraPositionState.centerInPath(path: Path) {
    val bounds = LatLngBounds.Builder().apply {
        path.points.forEach { include(it.toLatLng()) }
    }.build()
    this.animate(CameraUpdateFactory.newLatLngBounds(bounds, 16))
}

private suspend fun CameraPositionState.zoomInto(position: Position) {
    this.animate(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(position.toLatLng(), 17f)))
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

    val stopIconRes = SevIcons.SquareStopMarker.getByZoom(zoomLevel)
    val stopIcon = remember(stopIconRes) { BitmapDescriptorFactory.fromResource(stopIconRes) }

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
                    zIndex = 0.1f,
                )
            } else {
                Marker(
                    state = MarkerState(position = stop.position.toLatLng()),
                    onClick = {
                        onStopSelected(stop)
                        false
                    },
                    zIndex = 1f
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

