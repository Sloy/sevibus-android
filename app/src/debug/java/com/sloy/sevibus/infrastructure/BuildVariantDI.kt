package com.sloy.sevibus.infrastructure

import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.sloy.debugmenu.events.EventStore
import com.sloy.debugmenu.events.EventsDebugModuleDataSource
import com.sloy.sevibus.feature.debug.events.OverlayTracker
import com.sloy.sevibus.feature.debug.network.overlay.HttpOverlayInterceptor
import com.sloy.sevibus.infrastructure.analytics.Tracker
import com.sloy.sevibus.infrastructure.location.DebugLocationService
import com.sloy.sevibus.infrastructure.location.FusedLocationService
import com.sloy.sevibus.infrastructure.location.LocationService
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

object BuildVariantDI {
    private val loggingInterceptor = HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) }

    val module = module {
        single<LocationService> { DebugLocationService(get<FusedLocationService>(), get()) }

        single { EventStore() }
        single { EventsDebugModuleDataSource(androidContext()) }
        single { OverlayTracker(get(), get(), get()) }.bind(Tracker::class)

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
            HttpOverlayInterceptor(get(), get())
        ) }
    }
}
