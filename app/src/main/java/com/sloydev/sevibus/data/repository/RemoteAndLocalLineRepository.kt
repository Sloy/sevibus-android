package com.sloydev.sevibus.data.repository

import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.data.database.LineEntity
import com.sloydev.sevibus.data.database.RouteEntity
import com.sloydev.sevibus.data.database.TussamDao
import com.sloydev.sevibus.data.database.fromEntity
import com.sloydev.sevibus.data.database.toEntity
import com.sloydev.sevibus.domain.model.Line
import com.sloydev.sevibus.domain.model.LineId
import com.sloydev.sevibus.domain.repository.LineRepository

class RemoteAndLocalLineRepository(
    //private val api: SevibusApi,
    private val dao: TussamDao
) : LineRepository {

    override suspend fun obtainLines(): List<Line> {
        val lines: List<LineEntity> = dao.getLines()
            .ifEmpty {
                val remote = Stubs.lines
                dao.putLines(remote.map { it.toEntity() })
                dao.getLines()
            }
        val routes: List<RouteEntity> = dao.getRoutes()
            .ifEmpty {
                val remote = Stubs.routes
                dao.putRoutes(remote.map { it.toEntity() })
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


