package com.sloy.sevibus.data.repository

import com.sloy.sevibus.data.api.SevibusApi
import com.sloy.sevibus.data.api.model.PositionDto
import com.sloy.sevibus.data.api.model.StopDto
import com.sloy.sevibus.data.database.StopEntity
import com.sloy.sevibus.data.database.TussamDao
import com.sloy.sevibus.domain.model.LineSummary
import com.sloy.sevibus.domain.model.Position
import com.sloy.sevibus.domain.model.Stop
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.domain.model.toSummary
import com.sloy.sevibus.domain.repository.LineRepository
import com.sloy.sevibus.domain.repository.StopRepository
import com.sloy.sevibus.infrastructure.SevLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class RemoteAndLocalStopRepository(
    private val api: SevibusApi,
    private val dao: TussamDao,
    private val lineRepository: LineRepository,
    shouldAutoUpdate: Boolean = true
) : StopRepository {

    private val mutex = Mutex()
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var isDataFresh = false

    init {
        if (shouldAutoUpdate) {
            refreshLocalDataAsync()
        }
    }

    override suspend fun obtainStops(): List<Stop> = withContext(Dispatchers.Default) {
        val lines = async { lineRepository.obtainLines().map { it.toSummary() } }
        val stops = async {
            dao.getStops().ifEmpty {
                refreshLocalData()
                dao.getStops()
            }
        }
        return@withContext stops.await().map { entity ->
            entity.fromEntity(lines.await().filter { it.id in entity.lines })
        }
    }

    override suspend fun obtainStops(ids: List<StopId>): List<Stop> = withContext(Dispatchers.Default) {
        val stopEntities = dao.getStops(ids).sortedBy { ids.indexOf(it.code) }
        val lines = lineRepository.obtainLines(stopEntities.flatMap { it.lines }).map { it.toSummary() }
        return@withContext stopEntities.map { entity -> entity.fromEntity(lines.filter { it.id in entity.lines }) }
    }

    override suspend fun obtainStop(id: StopId): Stop = withContext(Dispatchers.Default) {
        val stopEntity = dao.getStop(id)
        val lines = lineRepository.obtainLines(stopEntity.lines).map { it.toSummary() }
        return@withContext stopEntity.fromEntity(lines)
    }

    override suspend fun searchStops(query: String): List<Stop> = withContext(Dispatchers.Default) {
        val formattedQuery = query.trim().split(" ").joinToString(" ") { "$it*" }
        val stops = dao.searchStops(formattedQuery)
        val lines = lineRepository.obtainLines(stops.flatMap { it.lines }).map { it.toSummary() }
        return@withContext stops.map { entity ->
            entity.fromEntity(lines.filter { it.id in entity.lines })
        }
    }

    /**
     * This function should be called when the local data is empty, to force a refresh and wait for the response to be cached.
     * For background updates without stopping the current coroutine use [refreshLocalDataAsync]
     */
    private suspend fun refreshLocalData() {
        runCatching {
            mutex.withLock {
                if (isDataFresh) return
                SevLogger.logD("Refreshing local data for STOPS")
                val remote = api.getStops()
                dao.putStops(remote.map { it.toEntity() })
                isDataFresh = true
            }
        }.onFailure { SevLogger.logE(it, "Error refreshing Stop local data") }
    }

    /**
     * This function refreshes the local data in the background, without blocking the current coroutine.
     * It's intended for fire-and-forget updates when the local data is suspected to be outdated but we want to return the
     * existing local data for a smooth user experience.
     */
    private fun refreshLocalDataAsync() {
        if (isDataFresh) return
        backgroundScope.launch {
            refreshLocalData()
        }
    }
}

private fun StopEntity.fromEntity(lines: List<LineSummary>): Stop {
    return Stop(code, description, position, lines)
}

private fun StopDto.toEntity(): StopEntity {
    return StopEntity(code, description, position.fromDto(), lines)
}

fun PositionDto.fromDto(): Position {
    return Position(this.latitude, this.longitude)
}
