package com.sloy.debugmenu.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloy.debugmenu.overlay.OverlayLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class NetworkDebugModuleViewModel(
    private val dataSource: NetworkDebugModuleDataSource,
    private val overlayLogger: OverlayLogger,
    val hostPresets: List<HostPreset>,
    private val httpCache: Cache? = null,
    private val healthCheck: (suspend () -> Map<String, String>)? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    val state: StateFlow<NetworkDebugModuleState> = dataSource.observeCurrentState()

    private val _toolsState = MutableStateFlow(NetworkToolsState())
    val toolsState: StateFlow<NetworkToolsState> = _toolsState.asStateFlow()

    val isHealthCheckAvailable: Boolean = healthCheck != null

    init {
        if (httpCache != null) refreshCacheSize(justCleared = false)
    }

    fun onHttpOverlayToggled(enabled: Boolean) {
        update { copy(isHttpOverlayEnabled = enabled) }
        if (!enabled) {
            overlayLogger.clear(HttpOverlayLoggerItem::class)
        }
    }

    fun onForceFailureToggled(enabled: Boolean) = update { copy(isForceFailureEnabled = enabled) }

    fun onAutoResetToggled(enabled: Boolean) = update { copy(autoResetForceFailure = enabled) }

    fun onLatencySelected(preset: LatencyPreset) = update { copy(latencyPreset = preset) }

    fun onHostSelected(url: String) {
        val defaultOrigin = hostPresets.firstOrNull()?.url?.toHttpUrlOrNull()?.origin()
        val selectedOrigin = url.toHttpUrlOrNull()?.origin()
        val isDefault = defaultOrigin != null && defaultOrigin == selectedOrigin
        update { copy(hostOverride = if (isDefault) null else url) }
    }

    fun onCustomHostApplied(input: String): Boolean {
        val url = input.trim().toHttpUrlOrNull() ?: return false
        update { copy(hostOverride = url.origin()) }
        return true
    }

    fun onClearHttpCacheClicked() {
        val cache = httpCache ?: return
        _toolsState.update { it.copy(httpCache = HttpCacheState.Loading) }
        viewModelScope.launch {
            runCatching { withContext(ioDispatcher) { cache.evictAll() } }
                .onSuccess { refreshCacheSize(justCleared = true) }
                .onFailure { error -> _toolsState.update { it.copy(httpCache = HttpCacheState.Error(error.toString())) } }
        }
    }

    fun onHealthCheckClicked() {
        val check = healthCheck ?: return
        _toolsState.update { it.copy(healthCheck = HealthCheckState.Loading) }
        viewModelScope.launch {
            val result = runCatching { check() }
                .fold(onSuccess = { HealthCheckState.Success(it) }, onFailure = { HealthCheckState.Error(it.toString()) })
            _toolsState.update { it.copy(healthCheck = result) }
        }
    }

    private fun refreshCacheSize(justCleared: Boolean) {
        val cache = httpCache ?: return
        viewModelScope.launch {
            val result = runCatching { withContext(ioDispatcher) { cache.size() } }
                .fold(onSuccess = { HttpCacheState.Ready(it, justCleared) }, onFailure = { HttpCacheState.Error(it.toString()) })
            _toolsState.update { it.copy(httpCache = result) }
        }
    }

    private fun update(transform: NetworkDebugModuleState.() -> NetworkDebugModuleState) {
        dataSource.updateState(dataSource.getCurrentState().transform())
    }
}

internal fun hostSelectionIndex(hostOverride: String?, presets: List<HostPreset>): Int {
    if (hostOverride == null) return 0
    val override = hostOverride.toHttpUrlOrNull()?.origin()
    val presetIndex = presets.indexOfFirst { it.url.toHttpUrlOrNull()?.origin() == override }
    return if (presetIndex >= 0) presetIndex else presets.size
}

private fun HttpUrl.origin(): String =
    newBuilder().encodedPath("/").query(null).fragment(null).build().toString().trimEnd('/')
