package com.sloydev.sevibus.domain.model

data class RoutePath(
    val route: RouteId,
    val points: List<Stop.Position>
)
