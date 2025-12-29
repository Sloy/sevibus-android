package com.sloy.sevibus.data.repository

import com.sloy.sevibus.data.database.SevibusDao
import com.sloy.sevibus.data.database.fromEntity
import com.sloy.sevibus.data.database.toEntity
import com.sloy.sevibus.domain.model.FavoriteStop
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.domain.repository.FavoriteRepository
import com.sloy.sevibus.domain.repository.StopRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LocalFavoriteRepository(
    private val sevibusDao: SevibusDao,
    private val stopRepository: StopRepository
) : FavoriteRepository {
    override fun observeFavorites(): Flow<List<FavoriteStop>> {
        return sevibusDao.observeFavorites()
            .map { entities ->
                entities.map { entity ->
                    entity.fromEntity(stopRepository.obtainStop(entity.stopId))
                }
            }
    }

    override suspend fun obtainFavorites(): List<FavoriteStop> = withContext(Dispatchers.Default) {
        sevibusDao.getFavorites().map { entity ->
            async {
                entity.fromEntity(stopRepository.obtainStop(entity.stopId))
            }
        }.awaitAll()
    }

    override suspend fun isFavorite(stop: StopId): Boolean {
        return obtainFavorites().any { it.stop.code == stop }
    }

    override suspend fun addFavorite(stop: FavoriteStop) {
        val lastOrder = sevibusDao.getFavorites().sortedBy { it.order }.lastOrNull()?.order ?: -1
        sevibusDao.putFavorite(stop.toEntity(order = lastOrder + 1))
    }

    override suspend fun removeFavorite(stop: StopId) {
        sevibusDao.deleteFavorite(stop)
    }

    override suspend fun replaceFavorites(favorites: List<FavoriteStop>) {
        sevibusDao.replaceAllFavorites(favorites.mapIndexed { i, favorite -> favorite.toEntity(i) })
    }
}
