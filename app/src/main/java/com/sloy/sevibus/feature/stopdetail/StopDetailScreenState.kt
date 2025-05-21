package com.sloy.sevibus.feature.stopdetail

import com.sloy.sevibus.domain.model.BusArrival
import com.sloy.sevibus.domain.model.LineSummary
import com.sloy.sevibus.domain.model.Stop


sealed interface StopDetailScreenState {
    data object Loading : StopDetailScreenState
    data class Loaded(val stop: Stop, val isFavorite: Boolean = false, val arrivalsState: ArrivalsState = ArrivalsState.Loading(emptyList())) : StopDetailScreenState
    data class Failed(val throwable: Throwable) : StopDetailScreenState
}

sealed interface ArrivalsState {
    data class Loading(val lines: List<LineSummary>) : ArrivalsState
    data class Loaded(val arrivals: List<BusArrival>) : ArrivalsState
    data class Failed(val failedArrivals: List<BusArrival.NotAvailable>, val throwable: Throwable) : ArrivalsState
}
