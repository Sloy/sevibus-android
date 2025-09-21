package com.sloy.sevibus.infrastructure

import com.sloy.sevibus.feature.debug.tracking.TrackingDebugModuleDataSource
import okhttp3.Interceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

object BuildVariantDI {
    val module = module {
        single<List<Interceptor>> { emptyList() }
        single { TrackingDebugModuleDataSource(androidContext()) }
    }
}
