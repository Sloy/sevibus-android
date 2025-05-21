package com.sloy.sevibus.feature.foryou.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloy.sevibus.domain.model.FavoriteStop
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
        while (true) {
            runCatching {
                busRepository.obtainBusArrivals(favorite.stop.code)
                    .onePerLine()
            }.onSuccess { arrivals ->
                emit(FavoriteItemState.Loaded(favorite, arrivals))
            }.onFailure {
                SevLogger.logW(it, "Error loading arrivals for favorite stop ${favorite.stop.code}")
            }
            delay(20.seconds)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), FavoriteItemState.Loading(favorite))


}
