package com.sloy.sevibus.feature.map.layers

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.sloy.sevibus.domain.model.Bus
import com.sloy.sevibus.domain.model.Line
import com.sloy.sevibus.domain.model.LineColor
import com.sloy.sevibus.domain.model.Path
import com.sloy.sevibus.domain.model.Position
import com.sloy.sevibus.domain.model.Stop
import com.sloy.sevibus.domain.model.primary
import com.sloy.sevibus.domain.model.toLatLng
import com.sloy.sevibus.feature.map.ZoomLevel
import com.sloy.sevibus.feature.map.icons.BusMapIcon
import com.sloy.sevibus.feature.map.icons.CircularStopIcon
import com.sloy.sevibus.feature.map.icons.OutlinedStopIcon
import com.sloy.sevibus.feature.map.icons.ShapedStopIcon
import com.sloy.sevibus.feature.map.icons.rememberComposeBitmapDescriptor
import com.sloy.sevibus.feature.map.lineWidth
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
@GoogleMapComposable
fun GenericStopsMakerLayer(
    stops: List<Stop>,
    zoomLevel: ZoomLevel,
    onStopClick: (Stop) -> Unit,
    hideOnZoom: List<ZoomLevel> = emptyList(),
    colored: Boolean = true,
) {
    val stopColor = if (colored) SevTheme.colorScheme.primary else SevTheme.colorScheme.onSurfaceVariant
    val icon = rememberComposeBitmapDescriptor(stopColor, zoomLevel) {
        when (zoomLevel) {
            ZoomLevel.Close -> ShapedStopIcon(stopColor, 24.dp)
            ZoomLevel.Far -> CircularStopIcon(stopColor, 4.dp)
            ZoomLevel.Medium -> CircularStopIcon(stopColor, 8.dp)
        }
    }
    val isVisible = zoomLevel !in hideOnZoom
    if (!isVisible) return
    stops.forEach { stop ->
        // TODO replace with MarkerComposable when issue is resolved: https://github.com/googlemaps/android-maps-compose/issues/685
        Marker(
            state = rememberUpdatedMarkerState(position = stop.position.toLatLng()),
            anchor = Offset(0.5f, 0.8f),
            visible = isVisible,
            onClick = {
                onStopClick(stop)
                false
            },
            zIndex = MapZIndex.STOP_GENERIC,
            icon = icon
        )
    }
}

@Composable
@GoogleMapComposable
fun SelectedStopLayer(stop: Stop, zoomLevel: ZoomLevel, onStopClick: (Stop) -> Unit, color: LineColor? = null) {
    val stopColor = color?.primary() ?: SevTheme.colorScheme.primary
    MarkerComposable(
        keys = arrayOf(zoomLevel, stopColor),
        state = rememberUpdatedMarkerState(position = stop.position.toLatLng()),
        anchor = Offset(0.5f, 0.8f),
        onClick = {
            onStopClick(stop)
            false
        },
        zIndex = MapZIndex.STOP_SELECTED,
        content = {
            OutlinedStopIcon(stopColor, iconSize = 40.dp, shadow = true)
        }
    )
}

@Composable
@GoogleMapComposable
fun LineStopsMarkerLayer(lineStops: List<Stop>, line: Line, zoomLevel: ZoomLevel, onStopClick: (Stop) -> Unit) {
    val isVisible = zoomLevel > ZoomLevel.Far
    if (!isVisible) return
    val stopColor = line.color.primary()
    val icon = rememberComposeBitmapDescriptor(stopColor) { OutlinedStopIcon(stopColor, iconSize = 24.dp, shadow = false) }
    lineStops.forEach { stop ->
        Marker(
            state = rememberUpdatedMarkerState(position = stop.position.toLatLng()),
            anchor = Offset(0.5f, 0.8f),
            onClick = {
                onStopClick(stop)
                false
            },
            zIndex = MapZIndex.STOP_FROM_LINE,
            icon = icon,
        )
    }
}

@Composable
@GoogleMapComposable
fun SingleLineLayer(linePath: Path, zoomLevel: ZoomLevel, splitPoint: Position? = null) {
    val lineWidth = lineWidth(zoomLevel)
    LinePathPolyline(linePath, lineWidth, splitPoint)
}

@Composable
@GoogleMapComposable
fun MultipleLinesLayer(paths: List<Path>, zoomLevel: ZoomLevel, splitPoint: Position? = null) {
    val lineWidth = lineWidthThin(zoomLevel)
    paths.forEach { linePath ->
        LinePathPolyline(linePath, lineWidth, splitPoint)
    }
}


@Composable
@GoogleMapComposable
fun BusMarkersLayer(buses: List<Bus>, showLineTooltip: Boolean) {
    buses.forEach { bus ->
        MarkerComposable(
            keys = arrayOf(bus.line, showLineTooltip),
            state = rememberUpdatedMarkerState(position = bus.position.toLatLng()),
            onClick = { true },
            anchor = Offset(0.5f, 0.8f),
            zIndex = MapZIndex.BUS,
            content = {
                if (showLineTooltip) {
                    BusMapIcon(bus.line)
                } else {
                    BusMapIcon()
                }
            }
        )
    }
}


@Composable
@GoogleMapComposable
private fun lineWidth(zoomLevel: ZoomLevel): Float {
    with(LocalDensity.current) {
        return zoomLevel.lineWidth.toPx()
    }
}

@Composable
@GoogleMapComposable
private fun lineWidthThin(zoomLevel: ZoomLevel): Float {
    with(LocalDensity.current) {
        return zoomLevel.lineWidth.toPx() / 3
    }
}
