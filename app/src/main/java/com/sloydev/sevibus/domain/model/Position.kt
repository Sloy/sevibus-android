package com.sloydev.sevibus.domain.model

import com.google.android.gms.maps.model.LatLng
import kotlin.math.abs

data class Position(val latitude: Double, val longitude: Double)
data class PositionBounds(val northeast: Position, val southwest: Position)

fun Position.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

fun LatLng.fromLatLng(): Position {
    return Position(latitude, longitude)
}

fun Position.manhattanDistance(other: Position): Double {
    val latDiff = abs(this.latitude - other.latitude)
    val lonDiff = abs(this.longitude - other.longitude)
    return latDiff + lonDiff
}
