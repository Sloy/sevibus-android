package com.sloy.sevibus.feature.debug

import android.content.Intent
import com.sloy.debugmenu.network.NetworkDebugModuleDataSource
import com.sloy.debugmenu.network.applyLaunchHost
import com.sloy.sevibus.feature.debug.map.MapDebugModuleDataSource
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * Debug-only launch arguments used by the Maestro end-to-end suite.
 */
object DebugLaunchArguments : KoinComponent {
    const val EXTRA_API_HOST = "debugApiHost"
    const val EXTRA_MAP_MODE = "debugMapMode"
    const val EXTRA_HIDE_STOPS = "debugHideStops"
    const val EXTRA_HIDE_BUSES = "debugHideBuses"

    var mapMode: DebugMapMode = DebugMapMode.Full
        private set

    fun apply(intent: Intent) {
        get<NetworkDebugModuleDataSource>().applyLaunchHost(intent.getStringExtra(EXTRA_API_HOST))
        DebugMapMode.fromArgument(intent.getStringExtra(EXTRA_MAP_MODE))?.let { mapMode = it }
        applyMapMarkers(intent)
    }

    private fun applyMapMarkers(intent: Intent) {
        val dataSource = get<MapDebugModuleDataSource>()
        var state = dataSource.getCurrentState()
        intent.booleanExtraOrNull(EXTRA_HIDE_STOPS)?.let { state = state.copy(hideStops = it) }
        intent.booleanExtraOrNull(EXTRA_HIDE_BUSES)?.let { state = state.copy(hideBuses = it) }
        dataSource.updateState(state)
    }

    private fun Intent.booleanExtraOrNull(name: String): Boolean? =
        if (hasExtra(name)) getBooleanExtra(name, false) || getStringExtra(name).toBoolean() else null
}
