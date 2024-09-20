package com.sloy.sevibus.infrastructure.location

import android.location.Location
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.Position
import com.sloy.sevibus.feature.debug.LocationDebugModule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class DebugLocationService(val locationService: LocationService) : LocationService {
    val state = LocationDebugModule.locationState

    override fun requestLocationUpdates(): Flow<Location> {
        return locationService.requestLocationUpdates()
            .combine(state) { location, debugLocationState ->
                if (debugLocationState.isFakeLocation) {
                    location.setCoordinates(Stubs.locationTriana)
                } else {
                    location
                }
            }

    }

    override suspend fun obtainCurrentLocation(): Location? {
        val debugLocationState = state.first()
        return locationService.obtainCurrentLocation()?.let {
            if (debugLocationState.isFakeLocation) {
                it.setCoordinates(Stubs.locationTriana)
            } else {
                it
            }
        }
    }

    private fun Location.setCoordinates(position: Position): Location {
        this.latitude = position.latitude
        this.longitude = position.longitude
        return this
    }
}
