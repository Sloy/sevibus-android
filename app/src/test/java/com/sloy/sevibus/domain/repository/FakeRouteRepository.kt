package com.sloy.sevibus.domain.repository

import com.sloy.sevibus.domain.model.LineId
import com.sloy.sevibus.domain.model.Route
import com.sloy.sevibus.domain.model.RouteId
import com.sloy.sevibus.domain.model.StopId

class FakeRouteRepository : RouteRepository {

    private val routes = mutableListOf<Route>()

    override suspend fun obtainRoutes(): List<Route> {
        return routes
    }

    override suspend fun obtainRoutesOfStop(stop: StopId): List<Route> {
        return routes.filter { it.stops.contains(stop) }
    }

    override suspend fun obtainRoutesByLines(lines: List<LineId>): List<Route> {
        return routes.filter { it.line in lines }
    }

    override suspend fun obtainRoutesByLine(line: LineId): List<Route> {
        return obtainRoutesByLines(listOf(line))
    }

    override suspend fun obtainRoute(id: RouteId): Route {
        return routes.first { it.id == id }
    }

    override suspend fun obtainRoute(stopId: StopId, lineId: LineId): Route {
        return routes.first { it.line == lineId && it.stops.contains(stopId) }
    }

    fun setRoutes(routes: List<Route>) {
        this.routes.clear()
        this.routes.addAll(routes)
    }
}
