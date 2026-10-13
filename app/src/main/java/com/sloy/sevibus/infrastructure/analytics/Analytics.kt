package com.sloy.sevibus.infrastructure.analytics

import com.sloy.sevibus.infrastructure.analytics.events.UserProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class Analytics(
    private val trackers: List<Tracker>,
    private val analyticsSettingsDataSource: AnalyticsSettingsDataSource,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
) {
    private val dispatches = Channel<() -> Unit>(Channel.UNLIMITED)

    init {
        scope.launch {
            for (dispatch in dispatches) {
                if (analyticsSettingsDataSource.isAnalyticsEnabled()) dispatch()
            }
        }
    }

    fun track(event: SevEvent) {
        dispatches.trySend { trackers.forEach { it.track(event) } }
    }

    fun setUserProperty(property: UserProperty) {
        dispatches.trySend { trackers.forEach { it.setUserProperty(property) } }
    }
}
