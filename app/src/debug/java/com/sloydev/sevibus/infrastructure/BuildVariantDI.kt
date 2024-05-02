package com.sloydev.sevibus.infrastructure

import com.sloydev.sevibus.feature.debug.DebugModule
import com.sloydev.sevibus.feature.debug.LocationDebugModule
import com.sloydev.sevibus.infrastructure.location.DebugLocationService
import com.sloydev.sevibus.infrastructure.location.FusedLocationService
import com.sloydev.sevibus.infrastructure.location.LocationService
import org.koin.dsl.module

object BuildVariantDI {
    val module = module {
        single<List<DebugModule>> { listOf(LocationDebugModule()) }
        single<LocationService> { DebugLocationService(get<FusedLocationService>()) }
    }
}
