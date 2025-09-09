package com.sloy.sevibus.infrastructure

import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.sloy.sevibus.feature.debug.DebugModule
import com.sloy.sevibus.feature.debug.LocationDebugModule
import com.sloy.sevibus.feature.debug.http.DebugHttpOverlayState
import com.sloy.sevibus.feature.debug.http.HttpOverlayInterceptor
import com.sloy.sevibus.feature.debug.http.HttpOverlayState
import com.sloy.sevibus.infrastructure.location.DebugLocationService
import com.sloy.sevibus.infrastructure.location.FusedLocationService
import com.sloy.sevibus.infrastructure.location.LocationService
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

object BuildVariantDI {
    private val loggingInterceptor = HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) }
    
    val module = module {
        single<List<DebugModule>> { listOf(LocationDebugModule()) }
        single<LocationService> { DebugLocationService(get<FusedLocationService>()) }
        
        single<ChuckerCollector> {
            ChuckerCollector(
                context = androidContext(),
                showNotification = true,
                retentionPeriod = RetentionManager.Period.ONE_HOUR
            )
        }
        
        single { listOf(
            ChuckerInterceptor.Builder(androidContext())
                .collector(get<ChuckerCollector>())
                .maxContentLength(250_000L)
                .redactHeaders("Authorization", "Auth-Token", "Bearer")
                .alwaysReadResponseBody(false)
                .createShortcut(true)
                .build(),
            loggingInterceptor, 
            HttpOverlayInterceptor(get())
        ) }
        single<HttpOverlayState> { DebugHttpOverlayState(androidContext()) }
    }
}
