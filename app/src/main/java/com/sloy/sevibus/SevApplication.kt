package com.sloy.sevibus

import android.app.Application
import android.content.Context
import com.sloy.sevibus.feature.debug.DebugDI
import com.sloy.sevibus.infrastructure.AndroidLogger
import com.sloy.sevibus.infrastructure.BuildVariantDI
import com.sloy.sevibus.infrastructure.DI
import com.sloy.sevibus.infrastructure.SevLogger
import com.sloy.sevibus.infrastructure.analytics.Analytics
import com.sloy.sevibus.infrastructure.analytics.ScreenViewTracker
import com.sloy.sevibus.infrastructure.analytics.UserPropertiesTracker
import com.sloy.sevibus.infrastructure.analytics.events.Events
import com.sloy.sevibus.infrastructure.analytics.session.SessionTracker
import com.sloy.sevibus.infrastructure.config.RemoteConfigService
import com.sloy.sevibus.infrastructure.session.FirebaseAuthStorageRepair
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class SevApplication : Application() {

    private val remoteConfigService: RemoteConfigService by inject()
    private val analytics: Analytics by inject()
    private val screenViewTracker: ScreenViewTracker by inject()
    private val sessionTracker: SessionTracker by inject()
    private val userPropertiesTracker: UserPropertiesTracker by inject()

    private var repairedAuthStorage: Result<List<String>> = Result.success(emptyList())

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // Runs before content providers, so before Firebase is initialized
        repairedAuthStorage = runCatching { FirebaseAuthStorageRepair.repairIfNeeded(base) }
    }

    override fun onCreate() {
        super.onCreate()
        SevLogger.setLogger(AndroidLogger())
        repairedAuthStorage
            .onSuccess { if (it.isNotEmpty()) SevLogger.logW(msg = "Repaired orphan Firebase Auth storage: $it") }
            .onFailure { SevLogger.logE(it, "Failed to repair Firebase Auth storage") }
        startKoin {
            androidLogger()
            androidContext(this@SevApplication)
            modules(DI.viewModelModule, DI.dataModule, DI.infrastructureModule, BuildVariantDI.module, DebugDI.module)
        }

        remoteConfigService.initialize()

        analytics.track(Events.AppStarted)
        screenViewTracker.start()
        sessionTracker.start()
        userPropertiesTracker.start()
    }
}
