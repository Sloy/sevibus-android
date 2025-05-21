package com.sloy.sevibus.infrastructure.extensions

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState
import com.sloy.sevibus.domain.model.Position
import com.sloy.sevibus.domain.model.PositionBounds
import com.sloy.sevibus.domain.model.Stop
import com.sloy.sevibus.domain.model.fromLatLng
import com.sloy.sevibus.domain.model.toLatLng
import com.sloy.sevibus.feature.map.ZoomLevel


suspend fun CameraPositionState.centerStops(stops: List<Stop>) {
    val bounds = LatLngBounds.Builder().apply {
        stops.forEach { include(it.position.toLatLng()) }
    }.build()
    this.animate(CameraUpdateFactory.newLatLngBounds(bounds, 64))
}

suspend fun CameraPositionState.zoomInto(position: Position, zoomLevel: ZoomLevel = ZoomLevel.Close) {
    this.animate(
        CameraUpdateFactory.newCameraPosition(
            CameraPosition.fromLatLngZoom(
                position.toLatLng(),
                zoomLevel.minimumLevel.toFloat()
            )
        )
    )
}

val CameraPositionState.bounds: PositionBounds?
    get() = projection?.visibleRegion?.latLngBounds?.let {
        PositionBounds(it.northeast.fromLatLng(), it.southwest.fromLatLng())
    }
