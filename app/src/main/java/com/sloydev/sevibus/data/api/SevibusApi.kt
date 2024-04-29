package com.sloydev.sevibus.data.api

import com.sloydev.sevibus.data.api.model.BusArrivalDto
import com.sloydev.sevibus.data.api.model.LineDto
import com.sloydev.sevibus.data.api.model.RouteDto
import com.sloydev.sevibus.data.api.model.PathDto
import com.sloydev.sevibus.data.api.model.StopDto
import com.sloydev.sevibus.domain.model.RouteId
import com.sloydev.sevibus.domain.model.StopId
import retrofit2.http.GET
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

    @GET("path/{route}")
    suspend fun getPath(@Path("route") route: RouteId): PathDto

}