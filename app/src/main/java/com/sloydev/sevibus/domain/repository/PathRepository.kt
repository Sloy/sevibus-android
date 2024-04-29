package com.sloydev.sevibus.domain.repository

import com.sloydev.sevibus.domain.model.RouteId
import com.sloydev.sevibus.domain.model.Path

interface PathRepository {
    suspend fun obtainPath(routeId: RouteId): Path
}