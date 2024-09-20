package com.sloy.sevibus.data.repository

import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.FavoriteStop
import com.sloy.sevibus.domain.repository.FavoriteStopsRepository

class StubFavoriteStopsRepository : FavoriteStopsRepository {
    override suspend fun obtainFavorites(): List<FavoriteStop> {
        return Stubs.favorites
    }
}