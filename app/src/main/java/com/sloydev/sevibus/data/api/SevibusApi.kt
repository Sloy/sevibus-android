package com.sloydev.sevibus.data.api

import com.sloydev.sevibus.data.api.model.LineDto
import com.sloydev.sevibus.data.api.model.RouteDto
import com.sloydev.sevibus.data.api.model.StopDto

interface SevibusApi {
    fun getLines(): List<LineDto>
    fun getRoutes(): List<RouteDto>
    fun getStops(): List<StopDto>
}