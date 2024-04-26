package com.sloydev.sevibus.domain.repository

import com.sloydev.sevibus.domain.model.Stop
import com.sloydev.sevibus.domain.model.StopId

interface StopRepository {

    suspend fun obtainStops(ids: List<StopId>): List<Stop>
    suspend fun obtainStop(id: StopId): Stop
}

