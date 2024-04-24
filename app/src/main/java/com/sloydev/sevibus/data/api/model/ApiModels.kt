package com.sloydev.sevibus.data.api.model

import com.sloydev.sevibus.domain.model.LineId
import com.sloydev.sevibus.domain.model.RouteId
import com.sloydev.sevibus.domain.model.StopId

data class StopDto(
    val code: StopId,
    val description: String,
    val position: PositionDto,
    val lines: List<LineId>,
) {
    class PositionDto(val latitude: Double, val longitude: Double)
}

data class LineDto(
    val label: String,
    val description: String,
    val colorHex: Long,
    val group: String,
    val routes: List<RouteId>,
    val id: LineId,
)

data class RouteDto(
    val id: RouteId,
    val direction: Int,
    val destination: String,
    val line: LineId,
    val stops: List<StopId>,
    val schedule: ScheduleDto?,
) {
    data class ScheduleDto(
        val startTime: String,
        val endTime: String,
    )
}