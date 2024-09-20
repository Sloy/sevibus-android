package com.sloy.sevibus.feature.foryou.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloy.sevibus.domain.model.BusArrival
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.domain.repository.BusRepository
import com.sloy.sevibus.domain.repository.FavoriteStopsRepository
import com.sloy.sevibus.infrastructure.SevLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val favoriteRepository: FavoriteStopsRepository,
    private val busRepository: BusRepository,
) : ViewModel() {

    val state = MutableStateFlow<FavoriteSubScreenState>(FavoriteSubScreenState.Initial)

    init {
        viewModelScope.launch {
            runCatching {
                val favorites = favoriteRepository.obtainFavorites()
                state.value = FavoriteSubScreenState.Content.LoadingArrivals(favorites)

                val allArrivals: Map<StopId, List<BusArrival>> =
                    favorites.associate { favorite -> favorite.stop.code to busRepository.obtainBusArrivals(favorite.stop.code).onePerLine() }

                state.value = FavoriteSubScreenState.Content.WithArrivals(favorites, allArrivals)
            }.onFailure { SevLogger.logW(it) }
        }
    }
}

private fun List<BusArrival>.onePerLine(): List<BusArrival> = this.distinctBy { it.line }