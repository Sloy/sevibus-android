package com.sloydev.sevibus.domain.repository

import com.sloydev.sevibus.domain.model.LineId
import com.sloydev.sevibus.domain.model.RouteWithStops

interface StopRepository {

    suspend fun obtainRouteStops(line: LineId): List<RouteWithStops>
}

