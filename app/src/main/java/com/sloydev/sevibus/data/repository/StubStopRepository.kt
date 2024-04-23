package com.sloydev.sevibus.data.repository

import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.domain.model.Stop
import com.sloydev.sevibus.domain.model.StopId
import com.sloydev.sevibus.domain.repository.StopRepository
import kotlinx.coroutines.delay

class StubStopRepository : StopRepository {

    override suspend fun obtainStops(ids: List<StopId>): List<Stop> {
        delay(500)
        return Stubs.stops.shuffled()
    }
}