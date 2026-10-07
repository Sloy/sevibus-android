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

val SEVILLA_CENTER = LatLng(37.3886, -5.9900)

private val SEVILLA_NORTHWEST_CORNER = LatLng(37.472174, -6.043031)
private val SEVILLA_SOUTHEAST_CORNER = LatLng(37.289951, -5.822207)
val SEVILLA_BOUNDS = LatLngBounds.builder()
    .include(SEVILLA_NORTHWEST_CORNER)
    .include(SEVILLA_SOUTHEAST_CORNER)
    .build()

/**
 * Where the map camera target can go. Google Maps applies the restriction to the center of the whole map, not of its
 * padded area, so with the sheet open and the camera zoomed out the center can be well south of what the user sees.
 * The margin keeps it from pushing the camera north when a line reaches the edge of Sevilla.
 */
private const val CAMERA_TARGET_MARGIN = 0.15
val SEVILLA_CAMERA_TARGET_BOUNDS = LatLngBounds.builder()
    .include(LatLng(SEVILLA_NORTHWEST_CORNER.latitude + CAMERA_TARGET_MARGIN, SEVILLA_NORTHWEST_CORNER.longitude - CAMERA_TARGET_MARGIN))
    .include(LatLng(SEVILLA_SOUTHEAST_CORNER.latitude - CAMERA_TARGET_MARGIN, SEVILLA_SOUTHEAST_CORNER.longitude + CAMERA_TARGET_MARGIN))
    .build()
