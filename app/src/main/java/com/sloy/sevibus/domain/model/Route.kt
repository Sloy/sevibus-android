package com.sloy.sevibus.domain.model

import java.time.LocalTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

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

fun Route.Schedule.isCurrentyActive(now: LocalTime = LocalTime.now()): Boolean {
    return if (endTime.isAfter(startTime)) {
        now.isAfter(startTime) && now.isBefore(endTime)
    } else {
        now.isAfter(startTime) || now.isBefore(endTime)
    }
}

/**
 * Like [isCurrentyActive] but widening the schedule by [margin] on both ends, to tolerate delays and schedule inaccuracies.
 * Schedules with the same start and end are considered unknown, so they're always active.
 */
fun Route.Schedule.isActiveWithMargin(now: LocalTime = LocalTime.now(), margin: Duration = 60.minutes): Boolean {
    if (startTime == endTime) return true
    val minutesInDay = 24 * 60
    val scheduleMinutes = Math.floorMod(endTime.toSecondOfDay() / 60 - startTime.toSecondOfDay() / 60, minutesInDay)
    if (scheduleMinutes + 2 * margin.inWholeMinutes >= minutesInDay) return true

    val start = startTime.minusMinutes(margin.inWholeMinutes)
    val end = endTime.plusMinutes(margin.inWholeMinutes)
    return if (end.isAfter(start)) {
        !now.isBefore(start) && !now.isAfter(end)
    } else {
        !now.isBefore(start) || !now.isAfter(end)
    }
}
