package com.sloydev.sevibus.domain.model

import com.google.android.gms.maps.model.LatLng

data class Stop(
    val code: StopId,
    val description: String,
    val position: Position,
    val lines: List<LineSummary>,
) {
    data class Position(val latitude: Double, val longitude: Double)
}

inline val Stop.description1: String
    get() = description.substringBeforeLast("(").trim()

val Stop.description2: String?
    get() {
        val description = description.substringAfterLast("(", "").substringBeforeLast(")", "").trim()
        return description.ifEmpty { null }
    }


typealias StopId = Int

@Deprecated("Just for development")
operator fun Stop.Position.plus(pos: Pair<Double, Double>): Stop.Position {
    return this.copy(latitude = latitude + pos.first, longitude = longitude + pos.second)
}

fun Stop.Position.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}
