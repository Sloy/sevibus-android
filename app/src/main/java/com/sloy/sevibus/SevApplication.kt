package com.sloy.sevibus

import android.app.Application
import com.sloy.sevibus.infrastructure.AndroidLogger
import com.sloy.sevibus.infrastructure.BuildVariantDI
import com.sloy.sevibus.infrastructure.DI
import com.sloy.sevibus.infrastructure.SevLogger
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class SevApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SevLogger.setLogger(AndroidLogger())
        startKoin {
            androidLogger()
            androidContext(this@SevApplication)
            modules(DI.viewModelModule, DI.dataModule, DI.infrastructureModule, BuildVariantDI.module)
        }
    }
}
