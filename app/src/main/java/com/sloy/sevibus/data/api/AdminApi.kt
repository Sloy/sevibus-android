package com.sloy.sevibus.data.api

import com.sloy.sevibus.data.api.model.HealthCheckDto
import retrofit2.http.GET

interface AdminApi {
    @GET("health")
    suspend fun healthCheck(): HealthCheckDto
}