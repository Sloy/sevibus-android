package com.sloydev.sevibus.data.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sloydev.sevibus.domain.model.Line
import com.sloydev.sevibus.domain.model.LineId
import com.sloydev.sevibus.domain.model.LineSummary
import com.sloydev.sevibus.domain.model.Route
import com.sloydev.sevibus.domain.model.RouteId
import com.sloydev.sevibus.domain.model.Stop
import com.sloydev.sevibus.domain.model.StopId

@Entity(tableName = "stops")
data class StopEntity(
    @PrimaryKey
    val code: StopId,
    val description: String,
    @Embedded
    val position: Stop.Position,
    val lines: List<LineId>,
)

@Entity(tableName = "lines")
data class LineEntity(
    @PrimaryKey
    val id: LineId,
    val label: String,
    val description: String,
    val colorHex: Long,
    val group: String,
    val routes: List<RouteId>,
)

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey
    val id: RouteId,
    val direction: Int,
    val destination: String,
    val line: LineId,
    @Embedded
    val schedule: Route.Schedule,
    val stops: List<StopId>,
) {
    /* data class Schedule(
         val startTime: LocalTime,
         val endTime: LocalTime
     )*/
}

fun LineEntity.fromEntity(routes: List<Route>): Line {
    return Line(label, description, colorHex, group, id = id, routes = routes)
}

fun LineEntity.summaryFromEntity(): LineSummary = LineSummary(id, label, colorHex)
fun Line.toEntity(): LineEntity {
    return LineEntity(id, label, description, colorHex, group, routes.map { it.id })
}


fun Route.toEntity(): RouteEntity {
    return RouteEntity(
        id, direction, destination, line, schedule, stops
//        RouteEntity.Schedule(schedule.startTime, schedule.endTime)
    )
}

fun RouteEntity.fromEntity(): Route {
    return Route(id, direction, destination, line, stops, schedule)
}