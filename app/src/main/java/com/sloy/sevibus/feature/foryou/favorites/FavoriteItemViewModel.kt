package com.sloy.sevibus.feature.foryou.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloy.sevibus.Stubs.arrivals
import com.sloy.sevibus.domain.model.BusArrival
import com.sloy.sevibus.domain.model.FavoriteStop
import com.sloy.sevibus.domain.model.LineId
import com.sloy.sevibus.domain.model.onePerLine
import com.sloy.sevibus.domain.repository.BusRepository
import com.sloy.sevibus.domain.repository.FavoriteRepository
import com.sloy.sevibus.infrastructure.SevLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

class FavoriteItemViewModel(
    private val favorite: FavoriteStop,
    private val favoriteRepository: FavoriteRepository,
    private val busRepository: BusRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<FavoriteItemState> = flow {
        if (favorite.selectedLineIds?.isEmpty() == true) {
            emit(FavoriteItemState.Loaded(favorite, emptyList()))
            return@flow
        }

        while (true) {
            runCatching {
                busRepository.obtainBusArrivals(favorite.stop.code)
                    .filter { it !is BusArrival.NotAvailable }
                    .filterBySelectedLines(favorite.selectedLineIds)
                    .onePerLine()
            }.onSuccess { arrivals ->
                emit(FavoriteItemState.Loaded(favorite, arrivals))
            }.onFailure {
                SevLogger.logW(it, "Error loading arrivals for favorite stop ${favorite.stop.code}")
            }
            delay(20.seconds)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), FavoriteItemState.Loading(favorite))

    private fun List<BusArrival>.filterBySelectedLines(selectedLineIds: Set<LineId>?): List<BusArrival> {
        return if (selectedLineIds == null) this else filter { arrival -> selectedLineIds.contains(arrival.line.id) }
    }


}
