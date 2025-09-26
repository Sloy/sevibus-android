package com.sloy.sevibus.feature.foryou.nearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloy.sevibus.domain.model.BusArrival
import com.sloy.sevibus.domain.model.onePerLine
import com.sloy.sevibus.domain.repository.BusRepository
import com.sloy.sevibus.infrastructure.SevLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

class NearbyItemViewModel(
    private val nearbyStop: NearbyStop,
    private val busRepository: BusRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<NearbyItemState> = flow {
        while (true) {
            runCatching {
                busRepository.obtainBusArrivals(nearbyStop.stop.code)
                    .filter { it !is BusArrival.NotAvailable }
                    .onePerLine()
            }.onSuccess { arrivals ->
                emit(NearbyItemState.Loaded(nearbyStop, arrivals))
            }.onFailure {
                SevLogger.logW(it, "Error loading arrivals for nearby stop ${nearbyStop.stop.code}")
            }
            delay(20.seconds)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), NearbyItemState.Loading(nearbyStop))


}
