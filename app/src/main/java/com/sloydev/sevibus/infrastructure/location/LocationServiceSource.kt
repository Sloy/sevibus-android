package com.sloydev.sevibus.infrastructure.location

import com.google.android.gms.maps.LocationSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LocationServiceSource(val locationService: LocationService) : LocationSource {

    private var job: Job? = null


    override fun activate(listener: LocationSource.OnLocationChangedListener) {
        job = CoroutineScope(Dispatchers.Main).launch {
            locationService.requestLocationUpdates()
                .collect { location ->
                    listener.onLocationChanged(location)
                }
        }
    }

    override fun deactivate() {
        job?.cancel()
    }
}
