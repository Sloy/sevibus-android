package com.sloy.sevibus.data.repository

import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.FavoriteStop
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class StubFavoriteRepository : FavoriteRepository {
    override fun observeFavorites(): Flow<List<FavoriteStop>> {
        return flow {
            obtainFavorites()
        }
    }

    override suspend fun obtainFavorites(): List<FavoriteStop> {
        return Stubs.favorites
    }

    override suspend fun isFavorite(stop: StopId): Boolean {
        return obtainFavorites().any { it.stop.code == stop }
    }

    override suspend fun addFavorite(stop: FavoriteStop) {
        TODO("Not yet implemented")
    }

    override suspend fun removeFavorite(stop: StopId) {
        TODO("Not yet implemented")
    }

    override suspend fun replaceFavorites(favorites: List<FavoriteStop>) {
        TODO("Not yet implemented")
    }
}
