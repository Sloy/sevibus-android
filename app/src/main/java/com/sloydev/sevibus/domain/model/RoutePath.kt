package com.sloydev.sevibus.domain.model

data class RoutePath(
    val routeId: RouteId,
    val points: List<Stop.Position>
)
