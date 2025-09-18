package com.sloy.sevibus.feature.foryou.nearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloy.sevibus.domain.model.toPosition
import com.sloy.sevibus.domain.usecase.ObtainNearbyStops
import com.sloy.sevibus.infrastructure.SevLogger
import com.sloy.sevibus.infrastructure.analytics.Analytics
import com.sloy.sevibus.infrastructure.analytics.SevEvent
import com.sloy.sevibus.infrastructure.location.LocationService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class NearbyViewModel(
    private val obtainNearbyStops: ObtainNearbyStops,
    private val locationService: LocationService,
    private val analytics: Analytics,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<NearbyScreenState> = locationService.requestLocationUpdates()
        .distinctUntilChanged()
        .map { obtainNearbyStops(it.toPosition()) }
        .map { NearbyScreenState.Content(it) }
        .catch { SevLogger.logE(it, "Error obtaining nearby stops") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), NearbyScreenState.Loading)

    fun onTrack(event: SevEvent) {
        analytics.track(event)
    }

}


