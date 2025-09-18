package com.sloy.sevibus.infrastructure.location

import android.location.Location
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.Position
import com.sloy.sevibus.feature.debug.location.LocationDebugModuleDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class DebugLocationService(
    private val locationService: LocationService,
    private val locationDebugModuleDataSource: LocationDebugModuleDataSource
) : LocationService {

    override fun requestLocationUpdates(): Flow<Location> {
        return locationService.requestLocationUpdates()
            .combine(locationDebugModuleDataSource.observeCurrentState()) { location, debugLocationState ->
                if (debugLocationState.isFakeLocationEnabled) {
                    location.setCoordinates(Stubs.locationTriana)
                } else {
                    location
                }
            }

    }

    override suspend fun obtainCurrentLocation(): Location? {
        val debugLocationState = locationDebugModuleDataSource.getCurrentState()
        return locationService.obtainCurrentLocation()?.let {
            if (debugLocationState.isFakeLocationEnabled) {
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
