package com.sloydev.sevibus.feature.stopdetail

import com.sloydev.sevibus.domain.model.BusArrival
import com.sloydev.sevibus.domain.model.Stop

data class StopDetailScreenState(
    val stopState: StopState = StopState.Loading,
    val arrivalsState: ArrivalsState = ArrivalsState.Loading,
)

sealed interface StopState {
    data object Loading : StopState
    data class Loaded(val stop: Stop) : StopState
    data class Failed(val throwable: Throwable) : StopState
}

sealed interface ArrivalsState {
    data object Loading : ArrivalsState
    data class Loaded(val arrivals: List<BusArrival>) : ArrivalsState
    data class Failed(val throwable: Throwable) : ArrivalsState
}
