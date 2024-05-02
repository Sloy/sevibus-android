package com.sloydev.sevibus.domain.model

typealias BusId = Int

data class Bus(
    val id: BusId,
    val position: Position,
    val positionInLine: Int,
    val direction: Int,
)
