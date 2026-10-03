package com.sloy.sevibus.feature.debug

import com.sloy.debugmenu.network.NetworkDebugModuleDataSource
import com.sloy.debugmenu.overlay.OverlayLogger
import com.sloy.debugmenu.overlay.OverlayLoggerImpl
import com.sloy.sevibus.feature.debug.auth.AuthDebugModuleDataSource
import com.sloy.sevibus.feature.debug.auth.AuthDebugModuleViewModel
import com.sloy.sevibus.feature.debug.inappreview.InAppReviewDebugModuleDataSource
import com.sloy.sevibus.feature.debug.inappreview.InAppReviewDebugModuleViewModel
import com.sloy.sevibus.feature.debug.location.LocationDebugModuleDataSource
import com.sloy.sevibus.feature.debug.location.LocationDebugModuleViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

object DebugDI {
    val module = module {
        viewModel { LocationDebugModuleViewModel(get()) }
        viewModel { InAppReviewDebugModuleViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { AuthDebugModuleViewModel(get(), get()) }
        single { LocationDebugModuleDataSource(androidContext()) }
        single { InAppReviewDebugModuleDataSource(androidContext()) }
        single { AuthDebugModuleDataSource(androidContext()) }
        single { NetworkDebugModuleDataSource(androidContext()) }
        single<OverlayLogger> { OverlayLoggerImpl() }
    }
}
