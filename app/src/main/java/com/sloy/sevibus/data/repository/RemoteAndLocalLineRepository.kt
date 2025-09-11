package com.sloy.sevibus.data.repository

import com.sloy.sevibus.data.api.SevibusApi
import com.sloy.sevibus.data.database.TussamDao
import com.sloy.sevibus.data.database.fromEntity
import com.sloy.sevibus.data.database.toEntity
import com.sloy.sevibus.domain.model.Line
import com.sloy.sevibus.domain.model.LineId
import com.sloy.sevibus.domain.repository.LineRepository
import com.sloy.sevibus.domain.repository.RouteRepository
import com.sloy.sevibus.infrastructure.SevLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class RemoteAndLocalLineRepository(
    private val api: SevibusApi,
    private val dao: TussamDao,
    private val routeRepository: RouteRepository,
    shouldAutoUpdate: Boolean = true
) : LineRepository {

    private val mutex = Mutex()
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var isDataFresh = false

    init {
        if (shouldAutoUpdate) {
            refreshLocalDataAsync()
        }
    }

    override suspend fun obtainLines(): List<Line> = withContext(Dispatchers.Default) {
        val lines = async {
            dao.getLines().ifEmpty {
                refreshLocalData()
                dao.getLines()
            }
        }
        val routes = async {
            routeRepository.obtainRoutes()
        }
        return@withContext lines.await().map { lineEntity ->
            lineEntity.fromEntity(routes.await().filter { it.line == lineEntity.id })
        }
    }

    override suspend fun obtainLines(ids: List<LineId>): List<Line> {
        val routes = routeRepository.obtainRoutesByLines(ids)
        val lines = dao.getLines(ids)
            .ifEmpty {
                refreshLocalData()
                dao.getLines(ids)
            }
        return lines.map { lineEntity -> lineEntity.fromEntity(routes.filter { it.line == lineEntity.id }) }
    }

    override suspend fun obtainLine(line: LineId): Line {
        val routes = routeRepository.obtainRoutesByLine(line)
        return dao.getLine(line).fromEntity(routes)
    }

    override suspend fun searchLines(query: String): List<Line> {
        val formattedQuery = query.trim().split(" ").joinToString(" ") { "$it*" }
        val lines = dao.searchLines(formattedQuery)
        val routes = routeRepository.obtainRoutesByLines(lines.map { it.id })
        return lines.map { lineEntity ->
            lineEntity.fromEntity(routes.filter { it.line == lineEntity.id })
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
                SevLogger.logD("Refreshing local data for LINES")
                val remote = api.getLines()
                dao.putLines(remote.map { it.toEntity() })
                isDataFresh = true
            }
        }.onFailure { SevLogger.logE(it, "Error refreshing Line local data") }
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


