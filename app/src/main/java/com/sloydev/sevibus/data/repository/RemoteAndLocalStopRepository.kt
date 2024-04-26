package com.sloydev.sevibus.data.repository

import com.sloydev.sevibus.data.api.SevibusApi
import com.sloydev.sevibus.data.api.model.StopDto
import com.sloydev.sevibus.data.database.StopEntity
import com.sloydev.sevibus.data.database.TussamDao
import com.sloydev.sevibus.data.database.summaryFromEntity
import com.sloydev.sevibus.domain.model.LineSummary
import com.sloydev.sevibus.domain.model.Stop
import com.sloydev.sevibus.domain.model.StopId
import com.sloydev.sevibus.domain.repository.StopRepository

class RemoteAndLocalStopRepository(
    private val api: SevibusApi,
    private val dao: TussamDao,
) : StopRepository {

    override suspend fun obtainStops(ids: List<StopId>): List<Stop> {
        //TODO inefficient as fuck!!
        val lines = dao.getLines().map { it.summaryFromEntity() }
        val stops = dao.getStops().ifEmpty {
            val remote = api.getStops()
            dao.putStops(remote.map { it.fromEntity() })
            dao.getStops()
        }
        return ids
            .map { id -> stops.first { it.code == id } }
            .map { entity ->
                entity
                    .fromEntity(lines.filter { it.id in entity.lines })
            }
    }

    override suspend fun obtainStop(id: StopId): Stop {
        val lines = dao.getLines().map { it.summaryFromEntity() }
        return dao.getStop(id).fromEntity(lines)
    }
}

private fun StopEntity.fromEntity(lines: List<LineSummary>): Stop {
    return Stop(code, description, position, lines)
}

private fun StopDto.fromEntity(): StopEntity {
    return StopEntity(code, description, position.fromDto(), lines)
}

fun StopDto.PositionDto.fromDto(): Stop.Position {
    return Stop.Position(this.latitude, this.longitude)
}