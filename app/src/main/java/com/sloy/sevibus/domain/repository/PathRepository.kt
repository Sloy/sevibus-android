package com.sloy.sevibus.domain.repository

import com.sloy.sevibus.domain.model.RouteId
import com.sloy.sevibus.domain.model.Path

interface PathRepository {
    suspend fun obtainPath(routeId: RouteId): Path
    suspend fun obtainPaths(routeIds: List<RouteId>): List<Path>
}