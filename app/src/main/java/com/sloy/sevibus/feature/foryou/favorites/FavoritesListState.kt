package com.sloy.sevibus.feature.foryou.favorites

import com.sloy.sevibus.domain.model.FavoriteStop

sealed interface FavoritesListState {
    data object Loading : FavoritesListState
    data object NotLogged : FavoritesListState
    data class Content(val favorites: List<FavoriteStop>) : FavoritesListState
}
