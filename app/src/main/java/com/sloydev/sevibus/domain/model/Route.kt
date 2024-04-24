package com.sloydev.sevibus.domain.model

import java.time.LocalTime

data class Route(
    val id: RouteId,
    val direction: Int,
    val destination: String,
    val line: LineId,
    val stops: List<StopId>,
    val schedule: Schedule? = null,
) {
    data class Schedule(
        val startTime: LocalTime,
        val endTime: LocalTime
    )
}
typealias RouteId = String

data class RouteWithStops(val route: Route, val stops: List<Stop>)