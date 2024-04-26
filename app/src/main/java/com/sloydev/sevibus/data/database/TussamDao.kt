package com.sloydev.sevibus.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sloydev.sevibus.domain.model.StopId

@Dao
interface TussamDao {
    @Query("SELECT * FROM lines")
    suspend fun getLines(): List<LineEntity>

    @Query("SELECT * FROM stops")
    suspend fun getStops(): List<StopEntity>

    @Query("SELECT * FROM stops WHERE code == :id")
    suspend fun getStop(id: StopId): StopEntity

    @Query("SELECT * FROM routes")
    suspend fun getRoutes(): List<RouteEntity>

    @Insert
    suspend fun putLines(lines: List<LineEntity>)

    @Insert
    suspend fun putStops(stops: List<StopEntity>)

    @Insert
    suspend fun putRoutes(routes: List<RouteEntity>)
}