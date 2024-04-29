package com.sloydev.sevibus.data.repository

import com.sloydev.sevibus.data.api.SevibusApi
import com.sloydev.sevibus.data.api.model.PathDto
import com.sloydev.sevibus.data.database.PathEntity
import com.sloydev.sevibus.data.database.TussamDao
import com.sloydev.sevibus.data.database.fromEntity
import com.sloydev.sevibus.domain.model.Path
import com.sloydev.sevibus.domain.model.RouteId
import com.sloydev.sevibus.domain.repository.PathRepository

class RemoteAndLocalPathRepository(
    private val api: SevibusApi,
    private val dao: TussamDao,
) : PathRepository {

    override suspend fun obtainPath(routeId: RouteId): Path {
        val local = dao.getPath(routeId)
        return if (local != null) {
            local.fromEntity()
        } else {
            val remote = api.getPath(routeId).toEntity()
            dao.putPath(remote)
            dao.getPath(routeId)!!.fromEntity()
        }
    }
}

private fun PathDto.toEntity(): PathEntity {
    return PathEntity(routeId, points)
}
