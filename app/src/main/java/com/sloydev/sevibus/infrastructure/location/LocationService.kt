package com.sloydev.sevibus.infrastructure.location

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationService {
    fun requestLocationUpdates(): Flow<Location>
    suspend fun obtainCurrentLocation(): Location?
}
