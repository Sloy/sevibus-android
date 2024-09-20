package com.sloy.sevibus.feature.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.JointType
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Polyline
import com.sloy.sevibus.domain.model.Path
import com.sloy.sevibus.domain.model.Stop
import com.sloy.sevibus.domain.model.moveToPath
import com.sloy.sevibus.domain.model.toLatLng
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
@GoogleMapComposable
fun MapLine(
    zoomLevel: Int,
    path: Path,
    selectedStop: Stop?
) {
    if (selectedStop == null) {
        MapLineOneColor(path, zoomLevel)
    } else {
        MapLineTwoColors(path, selectedStop, zoomLevel)
    }
}

@Composable
@GoogleMapComposable
private fun MapLineOneColor(path: Path, zoomLevel: Int) {
    val lineWidth = lineWidth(zoomLevel)
    Polyline(
        points = path.points.map { it.toLatLng() },
        color = SevTheme.colorScheme.primary,
        jointType = JointType.ROUND,
        width = lineWidth,
    )
}

@Composable
@GoogleMapComposable
private fun MapLineTwoColors(path: Path, selectedStop: Stop, zoomLevel: Int) {
    val lineWidth = lineWidth(zoomLevel)

    val splitPoint = selectedStop.position.moveToPath(path)
    val splitPointIndex = path.points.indexOf(splitPoint)
    val path1 = path.points.subList(0, splitPointIndex)
    val path2 = listOf(path1.last()) + path.points.subList(splitPointIndex, path.points.lastIndex)

    Polyline(
        points = path1.map { it.toLatLng() },
        color = SevTheme.colorScheme.primaryContainer,
        jointType = JointType.ROUND,
        width = lineWidth,
    )
    Polyline(
        points = path2.map { it.toLatLng() },
        color = SevTheme.colorScheme.primary,
        jointType = JointType.ROUND,
        width = lineWidth,
    )
}

@Composable
private fun lineWidth(zoomLevel: Int): Float {
    with(LocalDensity.current) {
        val lineWidth = if (zoomLevel < 14) {
            6.dp.toPx()
        } else {
            8.dp.toPx()
        }
        return lineWidth
    }
}
