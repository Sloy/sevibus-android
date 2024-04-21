package com.sloydev.sevibus.domain.model

import com.google.android.gms.maps.model.LatLng
import java.time.LocalTime

data class Stop(
    val code: Int,
    val description: String,
    val position: Position,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
) {
    data class Position(val latitude: Double, val longitude: Double)
}

@Deprecated("Just for development")
operator fun Stop.Position.plus(pos: Pair<Double, Double>): Stop.Position {
    return this.copy(latitude = latitude + pos.first, longitude = longitude + pos.second)
}

fun Stop.Position.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}
