package com.sloydev.sevibus.data.api

import com.sloydev.sevibus.data.api.model.LineDto
import com.sloydev.sevibus.data.api.model.RouteDto
import com.sloydev.sevibus.data.api.model.StopDto
import retrofit2.http.GET

interface SevibusApi {
    @GET("lines")
    suspend fun getLines(): List<LineDto>

    @GET("routes")
    suspend fun getRoutes(): List<RouteDto>

    @GET("stops")
    suspend fun getStops(): List<StopDto>
}