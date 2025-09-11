package com.sloy.sevibus.data.repository

import com.sloy.sevibus.data.api.SevibusApi
import com.sloy.sevibus.data.database.TussamDao
import com.sloy.sevibus.data.database.fromDto
import com.sloy.sevibus.data.database.fromEntity
import com.sloy.sevibus.data.database.toEntity
import com.sloy.sevibus.domain.model.Line
import com.sloy.sevibus.domain.model.LineSummary
import com.sloy.sevibus.domain.model.Path
import com.sloy.sevibus.domain.model.RouteId
import com.sloy.sevibus.domain.model.lineId
import com.sloy.sevibus.domain.model.toSummary
import com.sloy.sevibus.domain.repository.LineRepository
import com.sloy.sevibus.domain.repository.PathRepository
import com.sloy.sevibus.infrastructure.SevLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class RemoteAndLocalPathRepository(
    private val api: SevibusApi,
    private val dao: TussamDao,
    private val lineRepository: LineRepository,
    shouldAutoUpdate: Boolean = true
) : PathRepository {

    private val mutex = Mutex()
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var isDataFresh = false

    init {
        if (shouldAutoUpdate) {
            refreshLocalDataAsync()
        }
    }

    override suspend fun obtainPath(routeId: RouteId): Path {
        val line = lineRepository.obtainLine(routeId.lineId).toSummary()
        mutex.withLock {
            val local = dao.getPath(routeId)
            return if (local != null) {
                local.fromEntity(line)
            } else {
                val remote = api.getPath(routeId)
                dao.putPath(remote.toEntity())
                remote.fromDto(line)
            }
        }
    }

    override suspend fun obtainPaths(routeIds: List<RouteId>): List<Path> = withContext(Dispatchers.Default) {
        val paths = async {
            dao.getPaths(routeIds).ifEmpty {
                refreshLocalData()
                dao.getPaths(routeIds)
            }
        }
        val linesByRoute = async { lineRepository.obtainLines().byRouteId() }
        return@withContext paths.await().mapNotNull { entity ->
            val line = linesByRoute.await()[entity.routeId]
            if (line == null) SevLogger.logW(msg = "Failed to find route ${entity.routeId} for path")
            line?.let { entity.fromEntity(it) }
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
                SevLogger.logD("Refreshing local data for PATHS")
                val remote = api.getPaths()
                dao.putPaths(remote.map { it.toEntity() })
                isDataFresh = true
            }
        }.onFailure { SevLogger.logE(it, "Error refreshing Path local data") }
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

    private fun List<Line>.byRouteId(): Map<RouteId, LineSummary> {
        return this.map { line -> line.routes.map { it.id to line.toSummary() } }.flatten().toMap()
    }
}

