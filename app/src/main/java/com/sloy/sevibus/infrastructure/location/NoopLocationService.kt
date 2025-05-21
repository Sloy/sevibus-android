package com.sloy.sevibus.infrastructure.location

import android.location.Location
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.Position
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object NoopLocationService : LocationService {

    override fun requestLocationUpdates(): Flow<Location> {
        return flow {
            obtainCurrentLocation()
        }

    }

    override suspend fun obtainCurrentLocation(): Location? {
        return Location("fused").setCoordinates(Stubs.locationTriana).apply {
            this.accuracy = 10f
        }
    }

    private fun Location.setCoordinates(position: Position): Location {
        this.latitude = position.latitude
        this.longitude = position.longitude
        return this
    }
}
