package com.sloy.debugmenu.network

import androidx.lifecycle.ViewModel
import com.sloy.debugmenu.overlay.OverlayLogger
import kotlinx.coroutines.flow.StateFlow
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class NetworkDebugModuleViewModel(
    private val dataSource: NetworkDebugModuleDataSource,
    private val overlayLogger: OverlayLogger,
    val hostPresets: List<HostPreset>,
) : ViewModel() {

    val state: StateFlow<NetworkDebugModuleState> = dataSource.observeCurrentState()

    fun onHttpOverlayToggled(enabled: Boolean) {
        update { copy(isHttpOverlayEnabled = enabled) }
        if (!enabled) {
            overlayLogger.clear(HttpOverlayLoggerItem::class)
        }
    }

    fun onForceFailureToggled(enabled: Boolean) = update { copy(isForceFailureEnabled = enabled) }

    fun onAutoResetToggled(enabled: Boolean) = update { copy(autoResetForceFailure = enabled) }

    fun onLatencySelected(preset: LatencyPreset) = update { copy(latencyPreset = preset) }

    fun onHostSelected(url: String?) = update { copy(hostOverride = url) }

    fun onCustomHostApplied(input: String): Boolean {
        val url = input.trim().toHttpUrlOrNull() ?: return false
        update { copy(hostOverride = url.origin()) }
        return true
    }

    private fun update(transform: NetworkDebugModuleState.() -> NetworkDebugModuleState) {
        dataSource.updateState(dataSource.getCurrentState().transform())
    }
}

internal fun hostSelectionIndex(hostOverride: String?, presets: List<HostPreset>): Int {
    if (hostOverride == null) return 0
    val override = hostOverride.toHttpUrlOrNull()?.origin()
    val presetIndex = presets.indexOfFirst { it.url.toHttpUrlOrNull()?.origin() == override }
    return if (presetIndex >= 0) presetIndex + 1 else presets.size + 1
}

private fun HttpUrl.origin(): String =
    newBuilder().encodedPath("/").query(null).fragment(null).build().toString().trimEnd('/')
