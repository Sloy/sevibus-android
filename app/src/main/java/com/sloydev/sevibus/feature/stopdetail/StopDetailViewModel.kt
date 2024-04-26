package com.sloydev.sevibus.feature.stopdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloydev.sevibus.domain.model.StopId
import com.sloydev.sevibus.domain.repository.BusRepository
import com.sloydev.sevibus.domain.repository.StopRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class StopDetailViewModel(
    private val stopId: StopId,
    private val stopRepository: StopRepository,
    private val busRepository: BusRepository,
) : ViewModel() {

    val state = MutableStateFlow<StopDetailScreenState>(StopDetailScreenState.Initial)

    init {
        viewModelScope.launch {
            runCatching {
                val stop = async { stopRepository.obtainStop(stopId) }
                val arrivals = async { busRepository.obtainBusArrivals(stopId) }

                state.value = StopDetailScreenState.Content.LoadingArrivals(stop.await())
                state.value = StopDetailScreenState.Content.WithArrivals(stop.await(), arrivals.await())
            }
        }
    }

}