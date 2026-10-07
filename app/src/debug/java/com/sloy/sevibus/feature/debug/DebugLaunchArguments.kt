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
    const val EXTRA_MAP_MODE = "debugMapMode"

    var mapMode: DebugMapMode = DebugMapMode.Full
        private set

    fun apply(intent: Intent) {
        get<NetworkDebugModuleDataSource>().applyLaunchHost(intent.getStringExtra(EXTRA_API_HOST))
        DebugMapMode.fromArgument(intent.getStringExtra(EXTRA_MAP_MODE))?.let { mapMode = it }
    }
}
