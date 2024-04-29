package com.sloydev.sevibus.domain.model

data class Path(
    val routeId: RouteId,
    val points: List<Position>
)
