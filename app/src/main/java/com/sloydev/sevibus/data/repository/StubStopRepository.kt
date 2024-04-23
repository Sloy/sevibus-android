package com.sloydev.sevibus.data.repository

import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.domain.model.LineId
import com.sloydev.sevibus.domain.model.RouteWithStops
import com.sloydev.sevibus.domain.repository.StopRepository

class StubStopRepository : StopRepository {
    override suspend fun obtainRouteStops(line: LineId): List<RouteWithStops> {
        Stubs.delayNetwork()
        return Stubs.routesWithStops
    }
}