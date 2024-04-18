package com.sloydev.sevibus.feature.linestops

data class Stop(
    val code: Int,
    val description: String,
    val position: Position
) {
    data class Position(val latitude: Double, val longitude: Double)
}

