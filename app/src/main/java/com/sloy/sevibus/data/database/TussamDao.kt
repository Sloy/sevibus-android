package com.sloy.sevibus.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sloy.sevibus.domain.model.LineId
import com.sloy.sevibus.domain.model.RouteId
import com.sloy.sevibus.domain.model.StopId

@Dao
interface TussamDao {
    @Query("SELECT * FROM lines")
    suspend fun getLines(): List<LineEntity>

    @Query("SELECT * FROM lines WHERE id == :lineId")
    suspend fun getLine(lineId: LineId): LineEntity

    @Query("SELECT * FROM lines WHERE id IN (:lineIds)")
    suspend fun getLines(lineIds: List<LineId>): List<LineEntity>

    @Query(
        """
        SELECT lines.* FROM lines 
        JOIN lines_fts ON lines.id = lines_fts.rowid
        WHERE lines_fts.searchableText MATCH :query
    """
    )
    suspend fun searchLines(query: String): List<LineEntity>

    @Query("SELECT * FROM stops")
    suspend fun getStops(): List<StopEntity>

    @Query("SELECT * FROM stops WHERE code IN (:stopIds)")
    suspend fun getStops(stopIds: List<StopId>): List<StopEntity>

    @Query(
        """
        SELECT stops.* FROM stops 
        JOIN stops_fts ON stops.code = stops_fts.rowid
        WHERE stops_fts.searchableText MATCH :query
    """
    )
    suspend fun searchStops(query: String): List<StopEntity>

    @Query("SELECT * FROM stops WHERE code == :id")
    suspend fun getStop(id: StopId): StopEntity

    @Query("SELECT * FROM routes")
    suspend fun getRoutes(): List<RouteEntity>

    @Query("SELECT * FROM routes WHERE id == :routeId")
    suspend fun getRoute(routeId: RouteId): RouteEntity

    @Query("SELECT * FROM routes WHERE line IN (:lineIds)")
    suspend fun getRoutesByLines(lineIds: List<LineId>): List<RouteEntity>

    @Query("SELECT * FROM routes WHERE stops LIKE '%,' || :stopId || ',%' OR stops LIKE :stopId || ',%' OR stops LIKE '%,' || :stopId")
    suspend fun getRoutesByStop(stopId: StopId): List<RouteEntity>

    @Query("SELECT * FROM routes WHERE line == :lineId AND (stops LIKE '%,' || :stopId || ',%' OR stops LIKE :stopId || ',%' OR stops LIKE '%,' || :stopId)")
    suspend fun getRouteByStopAndLine(stopId: StopId, lineId: LineId): RouteEntity

    @Query("SELECT * FROM paths WHERE routeId == :id")
    suspend fun getPath(id: RouteId): PathEntity?

    @Query("SELECT * FROM paths WHERE routeId IN (:routeIds)")
    suspend fun getPaths(routeIds: List<RouteId>): List<PathEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putLines(lines: List<LineEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putStops(stops: List<StopEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putRoutes(routes: List<RouteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putPath(path: PathEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putPaths(paths: List<PathEntity>)

}
