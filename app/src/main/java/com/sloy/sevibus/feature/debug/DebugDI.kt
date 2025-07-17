package com.sloy.sevibus.feature.debug

import com.n26.debug.modules.tracking.NetworkDebugModuleDataSource
import com.n26.debug.modules.tracking.NetworkDebugModuleViewModel
import com.sloy.debugmenu.DebugMenuDataSource
import com.sloy.debugmenu.DebugMenuViewModel
import com.sloy.sevibus.feature.debug.location.LocationDebugModuleDataSource
import com.sloy.sevibus.feature.debug.location.LocationDebugModuleViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

object DebugDI {
    val module = module {
        viewModel { DebugMenuViewModel(get()) }
        viewModel { NetworkDebugModuleViewModel(get()) }
        viewModel { LocationDebugModuleViewModel(get()) }
        single { DebugMenuDataSource(get()) }
        single { NetworkDebugModuleDataSource(androidContext()) }
        single { LocationDebugModuleDataSource(androidContext()) }
    }

}
