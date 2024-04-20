package com.sloydev.sevibus.feature.linestops

data class Stop(
    val code: Int,
    val description: String,
    val position: Position
) {
    data class Position(val latitude: Double, val longitude: Double)
}

operator fun Stop.Position.plus(pos: Pair<Double, Double>): Stop.Position {
    return this.copy(latitude = latitude + pos.first, longitude = longitude + pos.second)
}

