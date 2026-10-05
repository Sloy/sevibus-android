package com.sloy.sevibus.feature.debug

import android.content.Intent
import com.sloy.debugmenu.network.NetworkDebugModuleDataSource
import com.sloy.debugmenu.network.applyLaunchHost
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * Debug-only launch arguments used by the Maestro end-to-end suite.
 */
object DebugLaunchArguments : KoinComponent {
    const val EXTRA_API_HOST = "debugApiHost"

    fun apply(intent: Intent) {
        get<NetworkDebugModuleDataSource>().applyLaunchHost(intent.getStringExtra(EXTRA_API_HOST))
    }
}
