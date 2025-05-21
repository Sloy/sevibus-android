package com.sloy.sevibus.data.repository

import com.sloy.sevibus.data.api.SevibusApi
import com.sloy.sevibus.data.api.model.PathDto
import com.sloy.sevibus.data.database.PathEntity
import com.sloy.sevibus.data.database.TussamDao
import com.sloy.sevibus.data.database.fromDto
import com.sloy.sevibus.data.database.fromEntity
import com.sloy.sevibus.domain.model.Path
import com.sloy.sevibus.domain.model.RouteId
import com.sloy.sevibus.domain.model.lineId
import com.sloy.sevibus.domain.model.toSummary
import com.sloy.sevibus.domain.repository.LineRepository
import com.sloy.sevibus.domain.repository.PathRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class RemoteAndLocalPathRepository(
    private val api: SevibusApi,
    private val dao: TussamDao,
    private val lineRepository: LineRepository,
) : PathRepository {

    override suspend fun obtainPath(routeId: RouteId): Path {
        val line = lineRepository.obtainLine(routeId.lineId).toSummary()

        val local = dao.getPath(routeId)
        return if (local != null) {
            local.fromEntity(line)
        } else {
            val remote = api.getPath(routeId)
            dao.putPath(remote.toEntity())
            remote.fromDto(line)
        }
    }

    override suspend fun obtainPaths(routeIds: List<RouteId>): List<Path> = withContext(Dispatchers.Default) {
        return@withContext routeIds.map { id ->
            async {
                runCatching { obtainPath(id) }.getOrNull()
            }
        }
            .awaitAll()
            .filterNotNull()
    }
}

private fun PathDto.toEntity(): PathEntity {
    return PathEntity(routeId, points)
}
