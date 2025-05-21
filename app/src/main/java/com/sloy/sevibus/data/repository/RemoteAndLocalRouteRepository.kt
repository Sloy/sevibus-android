package com.sloy.sevibus.data.repository

import com.sloy.sevibus.data.api.SevibusApi
import com.sloy.sevibus.data.database.TussamDao
import com.sloy.sevibus.data.database.fromEntity
import com.sloy.sevibus.domain.model.LineId
import com.sloy.sevibus.domain.model.Route
import com.sloy.sevibus.domain.model.RouteId
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.domain.repository.RouteRepository
import com.sloy.sevibus.infrastructure.SevLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class RemoteAndLocalRouteRepository(
    private val api: SevibusApi,
    private val dao: TussamDao,
    shouldAutoUpdate: Boolean = true
) : RouteRepository {

    private val mutex = Mutex()
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var isDataFresh = false

    init {
        if (shouldAutoUpdate) {
            refreshLocalDataAsync()
        }
    }

    override suspend fun obtainRoutes(): List<Route> = withContext(Dispatchers.Default) {
        return@withContext dao.getRoutes()
            .ifEmpty {
                refreshLocalData()
                dao.getRoutes()
            }.map { it.fromEntity() }
    }

    override suspend fun obtainRoutesByLine(line: LineId): List<Route> {
        return obtainRoutesByLines(listOf(line))
    }

    override suspend fun obtainRoutesOfStop(stop: StopId): List<Route> {
        return dao.getRoutesByStop(stop)
            .map { it.fromEntity() }
    }

    override suspend fun obtainRoutesByLines(lines: List<LineId>): List<Route> {
        return dao.getRoutesByLines(lines)
            .map { it.fromEntity() }
    }

    override suspend fun obtainRoute(id: RouteId): Route {
        return dao.getRoute(id).fromEntity()
    }

    override suspend fun obtainRoute(stopId: StopId, lineId: LineId): Route {
        return dao.getRouteByStopAndLine(stopId, lineId).fromEntity()
    }

    /**
     * This function should be called when the local data is empty, to force a refresh and wait for the response to be cached.
     * For background updates without stopping the current coroutine use [refreshLocalDataAsync]
     */
    private suspend fun refreshLocalData() {
        runCatching {
            mutex.withLock {
                if (isDataFresh) return
                SevLogger.logD("Refreshing local data for ROUTES")
                val remote = api.getRoutes()
                dao.putRoutes(remote.map { it.fromEntity() })
                isDataFresh = true
            }
        }.onFailure { SevLogger.logE(it, "Error refreshing Route local data") }
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
