package com.sloydev.sevibus.domain.repository

import com.sloydev.sevibus.domain.model.FavoriteStop

interface FavoriteStopsRepository {
    suspend fun obtainFavorites(): List<FavoriteStop>
}