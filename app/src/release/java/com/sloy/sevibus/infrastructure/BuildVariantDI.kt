package com.sloy.sevibus.infrastructure

import okhttp3.Interceptor
import org.koin.dsl.module

object BuildVariantDI {
    val module = module {
        single<List<Interceptor>> { emptyList() }
    }
}
