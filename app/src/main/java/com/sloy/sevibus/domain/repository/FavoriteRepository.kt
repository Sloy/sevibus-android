package com.sloy.sevibus.domain.repository

import com.sloy.sevibus.domain.model.FavoriteStop
import com.sloy.sevibus.domain.model.StopId
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun observeFavorites(): Flow<List<FavoriteStop>>
    suspend fun obtainFavorites(): List<FavoriteStop>
    suspend fun isFavorite(stop: StopId): Boolean
    suspend fun addFavorite(stop: FavoriteStop)
    suspend fun removeFavorite(stop: StopId)
    suspend fun replaceFavorites(favorites: List<FavoriteStop>)
}
