package com.sloy.sevibus.feature.debug

import com.sloy.sevibus.modules.tracking.NetworkDebugModuleDataSource
import com.sloy.sevibus.modules.tracking.NetworkDebugModuleViewModel
import com.sloy.debugmenu.overlay.OverlayLoggerStateHolder
import com.sloy.sevibus.feature.debug.location.LocationDebugModuleDataSource
import com.sloy.sevibus.feature.debug.location.LocationDebugModuleViewModel
import com.sloy.sevibus.feature.debug.auth.AuthDebugModuleDataSource
import com.sloy.sevibus.feature.debug.auth.AuthDebugModuleViewModel
import com.sloy.sevibus.feature.debug.inappreview.InAppReviewDebugModuleDataSource
import com.sloy.sevibus.feature.debug.inappreview.InAppReviewDebugModuleViewModel
import com.sloy.debugmenu.overlay.OverlayLoggerStateHolderImpl
import com.sloy.debugmenu.overlay.OverlayLogger
import com.sloy.debugmenu.overlay.OverlayLoggerImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

object DebugDI {
    val module = module {
        viewModel { NetworkDebugModuleViewModel(get(), get()) }
        viewModel { LocationDebugModuleViewModel(get()) }
        viewModel { InAppReviewDebugModuleViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { AuthDebugModuleViewModel(get(), get()) }
        single { NetworkDebugModuleDataSource(androidContext()) }
        single { LocationDebugModuleDataSource(androidContext()) }
        single { InAppReviewDebugModuleDataSource(androidContext()) }
        single { AuthDebugModuleDataSource(androidContext()) }
        single<OverlayLoggerStateHolder> { OverlayLoggerStateHolderImpl() }
        single<OverlayLogger> { OverlayLoggerImpl() }
    }

}
