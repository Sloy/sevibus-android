package com.sloy.debugmenu.network

/**
 * Transient state of the one-shot network tools. Not persisted.
 */
data class NetworkToolsState(
    val httpCache: HttpCacheState = HttpCacheState.Unavailable,
    val healthCheck: HealthCheckState = HealthCheckState.Idle,
)

sealed interface HttpCacheState {
    data object Unavailable : HttpCacheState
    data object Loading : HttpCacheState
    data class Ready(val sizeBytes: Long, val justCleared: Boolean = false) : HttpCacheState
    data class Error(val message: String) : HttpCacheState
}

sealed interface HealthCheckState {
    data object Idle : HealthCheckState
    data object Loading : HealthCheckState
    data class Success(val entries: Map<String, String>) : HealthCheckState
    data class Error(val message: String) : HealthCheckState
}
