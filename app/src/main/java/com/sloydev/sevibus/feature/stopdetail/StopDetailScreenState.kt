package com.sloydev.sevibus.feature.stopdetail

import com.sloydev.sevibus.domain.model.BusArrival
import com.sloydev.sevibus.domain.model.Stop

sealed interface StopDetailScreenState {
    data object Initial : StopDetailScreenState
    data object Error : StopDetailScreenState
    abstract class Content(
        open val stop: Stop,
    ) : StopDetailScreenState {
        data class LoadingArrivals(override val stop: Stop) : Content(stop)
        data class WithArrivals(override val stop: Stop, val arrivals: List<BusArrival>) : Content(stop)
    }
}