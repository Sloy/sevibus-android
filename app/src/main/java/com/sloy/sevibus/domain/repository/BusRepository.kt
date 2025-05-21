package com.sloy.sevibus.domain.repository

import com.sloy.sevibus.domain.model.Bus
import com.sloy.sevibus.domain.model.BusArrival
import com.sloy.sevibus.domain.model.RouteId
import com.sloy.sevibus.domain.model.StopId

interface BusRepository {
    suspend fun obtainBusArrivals(stop: StopId) : List<BusArrival>
    suspend fun obtainBuses(route: RouteId) : List<Bus>
}
