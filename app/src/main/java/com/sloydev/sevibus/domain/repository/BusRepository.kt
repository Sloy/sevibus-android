package com.sloydev.sevibus.domain.repository

import com.sloydev.sevibus.domain.model.BusArrival
import com.sloydev.sevibus.domain.model.StopId

interface BusRepository {
    suspend fun obtainBusArrivals(stop: StopId) : List<BusArrival>
}