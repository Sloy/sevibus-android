package com.sloydev.sevibus.domain.model

import com.google.android.gms.maps.model.LatLng

data class Position(val latitude: Double, val longitude: Double)

fun Position.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}
