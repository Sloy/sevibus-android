package com.sloy.sevibus.feature.stopdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.domain.repository.BusRepository
import com.sloy.sevibus.domain.repository.StopRepository
import com.sloy.sevibus.infrastructure.SevLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class StopDetailViewModel(
    private val stopId: StopId,
    private val stopRepository: StopRepository,
    private val busRepository: BusRepository,
) : ViewModel() {

    private val stopState = MutableStateFlow<StopState>(StopState.Loading)
    private val arrivalsState = MutableStateFlow<ArrivalsState>(ArrivalsState.Loading)

    val state = stopState.combine(arrivalsState) { stopState, arrivalsState -> StopDetailScreenState(stopState, arrivalsState) }

    init {
        viewModelScope.launch {
            runCatching {
                stopState.value = StopState.Loading
                val stop = stopRepository.obtainStop(stopId)
                stopState.value = StopState.Loaded(stop)
            }.onFailure {
                SevLogger.logW(it)
                stopState.value = StopState.Failed(it)
            }
        }
        viewModelScope.launch {
            refreshArrivals()
        }
    }

    private suspend fun refreshArrivals() {
        runCatching {
            val arrivals = busRepository.obtainBusArrivals(stopId)
            arrivalsState.value = ArrivalsState.Loaded(arrivals)
        }.onFailure {
            SevLogger.logW(it)
            arrivalsState.value = ArrivalsState.Failed(it)
        }
    }

}
