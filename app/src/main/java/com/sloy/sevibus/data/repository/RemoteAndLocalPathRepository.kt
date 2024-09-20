package com.sloy.sevibus.data.repository

import com.sloy.sevibus.data.api.SevibusApi
import com.sloy.sevibus.data.api.model.PathDto
import com.sloy.sevibus.data.database.PathEntity
import com.sloy.sevibus.data.database.TussamDao
import com.sloy.sevibus.data.database.fromEntity
import com.sloy.sevibus.domain.model.Path
import com.sloy.sevibus.domain.model.RouteId
import com.sloy.sevibus.domain.repository.PathRepository

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
