package com.sloy.sevibus.feature.debug

import com.sloy.debugmenu.overlay.NoopOverlayLogger
import com.sloy.debugmenu.overlay.OverlayLogger
import com.sloy.sevibus.feature.debug.inappreview.InAppReviewDebugModuleDataSource
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

object DebugDI {
    val module = module {
        single { InAppReviewDebugModuleDataSource(androidContext()) }
        single<OverlayLogger> { NoopOverlayLogger() }
    }
}
