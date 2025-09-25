package com.sloy.sevibus.domain.model

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlin.math.abs

data class Position(val latitude: Double, val longitude: Double)
data class PositionBounds(val northeast: Position, val southwest: Position)

fun Position.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

fun List<Position>.toBounds(): PositionBounds {
    val latitudes = this.map { it.latitude }
    val longitudes = this.map { it.longitude }
    val northeast = Position(latitudes.maxOrNull() ?: 0.0, longitudes.maxOrNull() ?: 0.0)
    val southwest = Position(latitudes.minOrNull() ?: 0.0, longitudes.minOrNull() ?: 0.0)
    return PositionBounds(northeast, southwest)
}

fun PositionBounds.toLatLngBounds(): LatLngBounds {
    return LatLngBounds(southwest.toLatLng(), northeast.toLatLng())
}

fun LatLng.fromLatLng(): Position {
    return Position(latitude, longitude)
}

fun Location.toPosition(): Position {
    return Position(latitude, longitude)
}

fun Location.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

fun LatLng.isInsideSevilla(): Boolean {
    return SEVILLA_BOUNDS.contains(this)
}

fun Position.manhattanDistance(other: Position): Double {
    val latDiff = abs(this.latitude - other.latitude)
    val lonDiff = abs(this.longitude - other.longitude)
    return latDiff + lonDiff
}

private val SEVILLA_NORTHWEST_CORNER = LatLng(37.425411, -6.013767)
private val SEVILLA_SOUTHEAST_CORNER = LatLng(37.338795, -5.892746)
val SEVILLA_BOUNDS = LatLngBounds.builder()
    .include(SEVILLA_NORTHWEST_CORNER)
    .include(SEVILLA_SOUTHEAST_CORNER)
    .build()
