package com.sloy.sevibus.domain.repository

import com.sloy.sevibus.domain.model.Stop
import com.sloy.sevibus.domain.model.StopId

interface StopRepository {

    suspend fun obtainStops(): List<Stop>
    suspend fun obtainStops(ids: List<StopId>): List<Stop>
    suspend fun obtainStop(id: StopId): Stop
}

