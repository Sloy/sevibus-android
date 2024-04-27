package com.sloydev.sevibus.data.repository

import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.domain.model.FavoriteStop
import com.sloydev.sevibus.domain.repository.FavoriteStopsRepository

class StubFavoriteStopsRepository : FavoriteStopsRepository {
    override suspend fun obtainFavorites(): List<FavoriteStop> {
        return Stubs.favorites
    }
}