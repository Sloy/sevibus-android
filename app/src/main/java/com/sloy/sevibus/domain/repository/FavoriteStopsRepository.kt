package com.sloy.sevibus.domain.repository

import com.sloy.sevibus.domain.model.FavoriteStop

interface FavoriteStopsRepository {
    suspend fun obtainFavorites(): List<FavoriteStop>
}