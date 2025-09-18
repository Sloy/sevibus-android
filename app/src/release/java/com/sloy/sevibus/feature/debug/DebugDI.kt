package com.sloy.sevibus.feature.debug

import com.sloy.debugmenu.overlay.NoopOverlayLoggerStateHolder
import com.sloy.debugmenu.overlay.OverlayLoggerStateHolder
import com.sloy.sevibus.modules.tracking.NetworkDebugModuleDataSource
import com.sloy.sevibus.feature.debug.inappreview.InAppReviewDebugModuleDataSource
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

object DebugDI {
    val module = module {
        single { NetworkDebugModuleDataSource() }
        single { InAppReviewDebugModuleDataSource(androidContext()) }
        single<OverlayLoggerStateHolder> { NoopOverlayLoggerStateHolder() }
    }
}