package com.sloydev.sevibus.data.api.model

import com.sloydev.sevibus.domain.model.BusId
import com.sloydev.sevibus.domain.model.LineColor
import com.sloydev.sevibus.domain.model.LineId
import com.sloydev.sevibus.domain.model.RouteId
import com.sloydev.sevibus.domain.model.StopId
import kotlinx.serialization.Serializable

@Serializable
data class StopDto(
    val code: StopId,
    val description: String,
    val position: PositionDto,
    val lines: List<LineId>,
)
@Serializable
class PositionDto(val latitude: Double, val longitude: Double)

@Serializable
data class LineDto(
    val label: String,
    val description: String,
    val color: LineColor,
    val group: String,
    val routes: List<RouteId>,
    val id: LineId,
)

@Serializable
data class RouteDto(
    val id: RouteId,
    val direction: Int,
    val destination: String,
    val line: LineId,
    val stops: List<StopId>,
    val schedule: ScheduleDto,
) {
    @Serializable
    data class ScheduleDto(
        val startTime: String,
        val endTime: String,
    )
}

@Serializable
data class BusArrivalDto(
    val bus: BusId,
    val distance: Int,
    val seconds: Int?,
    val line: LineId,
)

@Serializable
data class PathDto(
    val routeId: RouteId,
    val points: List<PositionDto>
)