package com.sloy.sevibus.infrastructure

import com.sloy.sevibus.feature.debug.http.HttpOverlayState
import com.sloy.sevibus.feature.debug.http.ReleaseHttpOverlayState
import okhttp3.Interceptor
import org.koin.dsl.module

object BuildVariantDI {
    val module = module {
        single<List<Interceptor>> { emptyList() }
        single<HttpOverlayState> { ReleaseHttpOverlayState() }
    }
}
