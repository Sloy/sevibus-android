package com.sloydev.sevibus.feature.foryou.favorites

import com.sloydev.sevibus.domain.model.BusArrival
import com.sloydev.sevibus.domain.model.FavoriteStop
import com.sloydev.sevibus.domain.model.StopId

sealed interface FavoriteSubScreenState {
    data object Initial : FavoriteSubScreenState
    data object Empty : FavoriteSubScreenState
    abstract class Content(open val favorites: List<FavoriteStop>) : FavoriteSubScreenState {
        data class LoadingArrivals(override val favorites: List<FavoriteStop>) : Content(favorites)
        data class WithArrivals(
            override val favorites: List<FavoriteStop>,
            val allArrivals: Map<StopId, List<BusArrival>>
        ) : Content(favorites)
    }
}