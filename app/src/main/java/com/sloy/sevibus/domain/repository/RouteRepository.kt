package com.sloy.sevibus.domain.repository

import com.sloy.sevibus.domain.model.LineId
import com.sloy.sevibus.domain.model.Route
import com.sloy.sevibus.domain.model.RouteId
import com.sloy.sevibus.domain.model.StopId

interface RouteRepository {
    suspend fun obtainRoutes(): List<Route>
    suspend fun obtainRoutesOfStop(stop: StopId): List<Route>
    suspend fun obtainRoutesByLine(line: LineId): List<Route>
    suspend fun obtainRoutesByLines(lines: List<LineId>): List<Route>
    suspend fun obtainRoute(id: RouteId): Route
    suspend fun obtainRoute(stopId: StopId, lineId: LineId): Route
}
