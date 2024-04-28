package com.sloydev.sevibus.domain.repository

import com.sloydev.sevibus.domain.model.RouteId
import com.sloydev.sevibus.domain.model.RoutePath

interface RoutePathRepository {
    suspend fun obtainPath(routeId: RouteId): RoutePath
}