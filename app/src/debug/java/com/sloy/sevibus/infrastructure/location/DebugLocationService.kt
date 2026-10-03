package com.sloy.sevibus.infrastructure.location

import android.location.Location
import com.sloy.sevibus.feature.debug.location.FakeLocation
import com.sloy.sevibus.feature.debug.location.LocationDebugModuleDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class DebugLocationService(
    private val locationService: LocationService,
    private val locationDebugModuleDataSource: LocationDebugModuleDataSource,
) : LocationService {

    override fun requestLocationUpdates(): Flow<Location> {
        return locationService.requestLocationUpdates()
            .combine(locationDebugModuleDataSource.observeCurrentState()) { location, state ->
                location.withFakeLocation(state.fakeLocation)
            }
    }

    override suspend fun obtainCurrentLocation(): Location? {
        val fakeLocation = locationDebugModuleDataSource.getCurrentState().fakeLocation
        return locationService.obtainCurrentLocation()?.withFakeLocation(fakeLocation)
    }

    private fun Location.withFakeLocation(fakeLocation: FakeLocation?): Location {
        if (fakeLocation == null) return this
        latitude = fakeLocation.position.latitude
        longitude = fakeLocation.position.longitude
        return this
    }
}
