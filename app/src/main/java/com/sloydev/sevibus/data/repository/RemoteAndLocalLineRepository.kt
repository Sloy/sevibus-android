package com.sloydev.sevibus.data.repository

import com.sloydev.sevibus.data.api.SevibusApi
import com.sloydev.sevibus.data.api.model.LineDto
import com.sloydev.sevibus.data.api.model.RouteDto
import com.sloydev.sevibus.data.database.LineEntity
import com.sloydev.sevibus.data.database.RouteEntity
import com.sloydev.sevibus.data.database.TussamDao
import com.sloydev.sevibus.data.database.fromEntity
import com.sloydev.sevibus.domain.model.Line
import com.sloydev.sevibus.domain.model.LineId
import com.sloydev.sevibus.domain.model.Route
import com.sloydev.sevibus.domain.repository.LineRepository
import java.time.LocalTime

class RemoteAndLocalLineRepository(
    private val api: SevibusApi,
    private val dao: TussamDao
) : LineRepository {

    override suspend fun obtainLines(): List<Line> {
        val lines: List<LineEntity> = dao.getLines()
            .ifEmpty {
                val remote = api.getLines()
                dao.putLines(remote.map { it.fromEntity() })
                dao.getLines()
            }
        val routes: List<RouteEntity> = dao.getRoutes()
            .ifEmpty {
                val remote = api.getRoutes()
                dao.putRoutes(remote.map { it.fromEntity() })
                dao.getRoutes()
            }

        return lines.map { lineEntity ->
            lineEntity.fromEntity(routes
                .filter { it.line == lineEntity.id }
                .map { it.fromEntity() }
            )
        }
    }

    override suspend fun obtainLine(line: LineId): Line {
        //TODO optimize to request 1 object to the DAO, or fallback to all lines
        return obtainLines().first { it.id == line }
    }
}

private fun RouteDto.fromEntity(): RouteEntity {
    return RouteEntity(id, direction, destination, line, schedule.fromDto(), stops)
}

private fun LineDto.fromEntity(): LineEntity {
    return LineEntity(id, label, description, color, group, routes)
}

private fun RouteDto.ScheduleDto.fromDto(): Route.Schedule {
    return Route.Schedule(
        startTime = LocalTime.parse(this.startTime),
        endTime = LocalTime.parse(this.endTime),
    )
}

