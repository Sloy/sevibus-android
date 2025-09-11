package com.sloy.sevibus.data.database

import com.sloy.sevibus.domain.model.LineId
import com.sloy.sevibus.domain.model.RouteId
import com.sloy.sevibus.domain.model.StopId

class FakeTussamDao : TussamDao {

    private val lines = mutableListOf<LineEntity>()
    private val stops = mutableListOf<StopEntity>()
    private val routes = mutableListOf<RouteEntity>()
    private val paths = mutableListOf<PathEntity>()

    override suspend fun getLines(): List<LineEntity> {
        return lines
    }

    override suspend fun getLines(lineIds: List<LineId>): List<LineEntity> {
        return lines.filter { it.id in lineIds }
    }

    override suspend fun getLine(lineId: LineId): LineEntity {
        return lines.first()
    }

    override suspend fun searchLines(query: String): List<LineEntity> {
        return lines
    }

    override suspend fun getStops(): List<StopEntity> {
        return stops
    }

    override suspend fun getStops(stopIds: List<StopId>): List<StopEntity> {
        return stops
    }

    override suspend fun searchStops(query: String): List<StopEntity> {
        return stops.filter { it.description.contains(query, ignoreCase = true) }
    }

    override suspend fun getStop(id: StopId): StopEntity {
        return stops.first { it.code == id }
    }

    override suspend fun getRoutes(): List<RouteEntity> {
        return routes
    }

    override suspend fun getRoute(routeId: RouteId): RouteEntity {
        return routes.first()
    }

    override suspend fun getRoutesByLines(lineIds: List<LineId>): List<RouteEntity> {
        return routes
    }

    override suspend fun getRoutesByStop(stopId: StopId): List<RouteEntity> {
        return routes
    }

    override suspend fun getRouteByStopAndLine(stopId: StopId, lineId: LineId): RouteEntity {
        return routes.first()
    }

    override suspend fun getPath(id: RouteId): PathEntity? {
        return paths.find { it.routeId == id }
    }

    override suspend fun getPaths(routeIds: List<RouteId>): List<PathEntity> {
        return paths.filter { it.routeId in routeIds }
    }

    override suspend fun putLines(lines: List<LineEntity>) {
        this.lines.clear()
        this.lines.addAll(lines)
    }

    override suspend fun putStops(stops: List<StopEntity>) {
        this.stops.clear()
        this.stops.addAll(stops)
    }

    override suspend fun putRoutes(routes: List<RouteEntity>) {
        this.routes.clear()
        this.routes.addAll(routes)
    }

    override suspend fun putPath(path: PathEntity) {
        paths.removeIf { it.routeId == path.routeId }
        paths.add(path)
    }

    override suspend fun putPaths(paths: List<PathEntity>) {
        paths.forEach { path ->
            this.paths.removeIf { it.routeId == path.routeId }
            this.paths.add(path)
        }
    }
}
