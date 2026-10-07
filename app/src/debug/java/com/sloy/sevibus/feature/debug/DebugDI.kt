package com.sloy.sevibus.feature.debug

import com.sloy.debugmenu.network.NetworkDebugModuleDataSource
import com.sloy.debugmenu.overlay.OverlayLogger
import com.sloy.debugmenu.overlay.OverlayLoggerImpl
import com.sloy.sevibus.feature.debug.auth.AuthDebugModuleViewModel
import com.sloy.sevibus.feature.debug.inappreview.InAppReviewDebugModuleDataSource
import com.sloy.sevibus.feature.debug.inappreview.InAppReviewDebugModuleViewModel
import com.sloy.sevibus.feature.debug.map.MapDebugModuleDataSource
import com.sloy.sevibus.feature.debug.map.MapDebugModuleViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

object DebugDI {
    val module = module {
        viewModel { MapDebugModuleViewModel(get()) }
        viewModel { InAppReviewDebugModuleViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { AuthDebugModuleViewModel(get()) }
        single { MapDebugModuleDataSource(androidContext()) }
        single { InAppReviewDebugModuleDataSource(androidContext()) }
        single { NetworkDebugModuleDataSource(androidContext()) }
        single<OverlayLogger> { OverlayLoggerImpl() }
    }
}
