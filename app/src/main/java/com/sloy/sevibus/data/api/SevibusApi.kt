package com.sloy.sevibus.data.api

import com.sloy.sevibus.data.api.model.BusArrivalDto
import com.sloy.sevibus.data.api.model.BusDto
import com.sloy.sevibus.data.api.model.CardInfoDto
import com.sloy.sevibus.data.api.model.CardTransactionDto
import com.sloy.sevibus.data.api.model.LineDto
import com.sloy.sevibus.data.api.model.PathChecksumRequestDto
import com.sloy.sevibus.data.api.model.PathDto
import com.sloy.sevibus.data.api.model.RouteDto
import com.sloy.sevibus.data.api.model.StopDto
import com.sloy.sevibus.domain.model.CardId
import com.sloy.sevibus.domain.model.RouteId
import com.sloy.sevibus.domain.model.StopId
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SevibusApi {
    @GET("lines")
    suspend fun getLines(): List<LineDto>

    @GET("routes")
    suspend fun getRoutes(): List<RouteDto>

    @GET("stops")
    suspend fun getStops(): List<StopDto>

    @GET("arrivals/{stop}")
    suspend fun getArrivals(@Path("stop") stop: StopId): List<BusArrivalDto>

    @GET("path/{route}?format=polyline")
    suspend fun getPath(@Path("route") route: RouteId): PathDto

    @GET("paths")
    suspend fun getPaths(): List<PathDto>

    @POST("path/checkNewAndUpdated?format=polyline")
    suspend fun getPathUpdatesOnly(@Body pathChecksums: List<PathChecksumRequestDto>): List<PathDto>

    @GET("buses/{route}")
    suspend fun getBuses(@Path("route") route: RouteId): List<BusDto>

    @GET("card/{card}")
    suspend fun getCardInfo(@Path("card") card: CardId): CardInfoDto

    @GET("card/{card}/transactions")
    suspend fun getCardTransactions(@Path("card") card: CardId): List<CardTransactionDto>
}
