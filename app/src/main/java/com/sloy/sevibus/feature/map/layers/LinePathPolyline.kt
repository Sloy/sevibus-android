package com.sloy.sevibus.feature.map.layers

import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Polyline
import com.sloy.sevibus.domain.model.Path
import com.sloy.sevibus.domain.model.Position
import com.sloy.sevibus.domain.model.moveToPath
import com.sloy.sevibus.domain.model.primary
import com.sloy.sevibus.domain.model.soft
import com.sloy.sevibus.domain.model.splitBy
import com.sloy.sevibus.domain.model.toLatLng

@Composable
@GoogleMapComposable
fun LinePathPolyline(linePath: Path, lineWidthPx: Float, splitPoint: Position? = null) {
    val splitPointOnPath = splitPoint?.moveToPath(linePath)
    val (dimPath, mainPath) = splitPointOnPath?.let { linePath.splitBy(it) } ?: Pair(emptyList(), linePath.points)
    if (dimPath.isNotEmpty()) {
        Polyline(
            points = dimPath.map { it.toLatLng() },
            color = linePath.line.color.soft(),
            jointType = JointType.ROUND,
            startCap = RoundCap(),
            endCap = RoundCap(),
            width = lineWidthPx,
            zIndex = MapZIndex.LINE
        )
    }
    if (mainPath.isNotEmpty()) {
        Polyline(
            points = mainPath.map { it.toLatLng() },
            color = linePath.line.color.primary(),
            jointType = JointType.ROUND,
            startCap = RoundCap(),
            endCap = RoundCap(),
            width = lineWidthPx,
            zIndex = MapZIndex.LINE
        )
    }
}
