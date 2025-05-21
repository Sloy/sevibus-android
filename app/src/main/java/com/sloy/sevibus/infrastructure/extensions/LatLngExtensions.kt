package com.sloy.sevibus.infrastructure.extensions

import com.google.android.gms.maps.model.LatLng
import kotlin.math.abs

fun LatLng.isApproximatelyEqualTo(other: LatLng, tolerance: Double = 0.0001): Boolean {
    val latDiff = abs(this.latitude - other.latitude)
    val lngDiff = abs(this.longitude - other.longitude)
    return (latDiff < tolerance && lngDiff < tolerance)

}