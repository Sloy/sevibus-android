package com.sloy.sevibus.domain.model

import java.time.LocalTime

data class Route(
    val id: RouteId,
    val direction: Int,
    val destination: String,
    val line: LineId,
    val stops: List<StopId>,
    val schedule: Schedule = Schedule(LocalTime.now(), LocalTime.now()),
) {
    data class Schedule(
        val startTime: LocalTime,
        val endTime: LocalTime
    )
}
typealias RouteId = String

val RouteId.lineId: LineId
    get() = this.split(".").first().toInt()
val RouteId.direction: Int
    get() = this.split(".").last().toInt()

data class RouteWithStops(val route: Route, val stops: List<Stop>)
