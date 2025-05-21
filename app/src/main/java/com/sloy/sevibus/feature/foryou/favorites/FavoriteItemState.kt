package com.sloy.sevibus.feature.foryou.favorites

import com.sloy.sevibus.domain.model.BusArrival
import com.sloy.sevibus.domain.model.FavoriteStop

sealed class FavoriteItemState(open val favorite: FavoriteStop) {
    data class Loading(override val favorite: FavoriteStop) : FavoriteItemState(favorite)
    data class Loaded(override val favorite: FavoriteStop, val arrivals: List<BusArrival>) : FavoriteItemState(favorite)

}
