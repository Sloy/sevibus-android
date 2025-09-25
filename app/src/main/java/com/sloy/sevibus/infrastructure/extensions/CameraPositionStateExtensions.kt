package com.sloy.sevibus.infrastructure.extensions

import com.google.maps.android.compose.CameraPositionState
import com.sloy.sevibus.domain.model.PositionBounds
import com.sloy.sevibus.domain.model.fromLatLng

val CameraPositionState.bounds: PositionBounds?
    get() = projection?.visibleRegion?.latLngBounds?.let {
        PositionBounds(it.northeast.fromLatLng(), it.southwest.fromLatLng())
    }
