package com.sloydev.sevibus.domain.repository

import com.sloydev.sevibus.domain.model.Bus
import com.sloydev.sevibus.domain.model.BusArrival
import com.sloydev.sevibus.domain.model.RouteId
import com.sloydev.sevibus.domain.model.StopId

interface BusRepository {
    suspend fun obtainBusArrivals(stop: StopId) : List<BusArrival>
    suspend fun obtainBuses(route: RouteId) : List<Bus>
}
