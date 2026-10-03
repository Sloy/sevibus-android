package com.sloy.sevibus.infrastructure.location

import android.content.Context
import android.location.Location
import com.sloy.sevibus.feature.debug.location.FakeLocation
import com.sloy.sevibus.feature.debug.location.LocationDebugModuleDataSource
import com.sloy.sevibus.feature.debug.location.LocationDebugModuleState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import strikt.api.expectThat
import strikt.assertions.isSameInstanceAs

class DebugLocationServiceTest {

    private val location: Location = mock()
    private val dataSource = LocationDebugModuleDataSource(mock<Context>(defaultAnswer = Mockito.RETURNS_DEEP_STUBS))
    private val service = DebugLocationService(FakeLocationService(location), dataSource)

    @Test
    fun `passes the real location through when no fake location is selected`() = runTest {
        val result = service.obtainCurrentLocation()

        expectThat(result).isSameInstanceAs(location)
        verify(location, never()).setLatitude(any())
        verify(location, never()).setLongitude(any())
    }

    @Test
    fun `current location uses the selected preset`() = runTest {
        dataSource.updateState(LocationDebugModuleState(fakeLocation = FakeLocation.Centro))

        service.obtainCurrentLocation()

        verify(location).setLatitude(37.3886)
        verify(location).setLongitude(-5.9953)
    }

    @Test
    fun `location updates use the selected preset`() = runTest {
        dataSource.updateState(LocationDebugModuleState(fakeLocation = FakeLocation.Huelva))

        service.requestLocationUpdates().first()

        verify(location).setLatitude(37.2614)
        verify(location).setLongitude(-6.9447)
    }

    private class FakeLocationService(private val location: Location) : LocationService {
        override fun requestLocationUpdates(): Flow<Location> = flowOf(location)
        override suspend fun obtainCurrentLocation(): Location = location
    }
}
