package com.sloy.sevibus.infrastructure.analytics

import com.sloy.sevibus.infrastructure.analytics.events.UserProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class Analytics(
    private val trackers: List<Tracker>,
    private val analyticsSettingsDataSource: AnalyticsSettingsDataSource
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun track(event: SevEvent) {
        scope.launch {
            if (analyticsSettingsDataSource.isAnalyticsEnabled()) {
                trackers.forEach { it.track(event) }
            }
        }
    }

    fun setUserProperty(property: UserProperty) {
        scope.launch {
            if (analyticsSettingsDataSource.isAnalyticsEnabled()) {
                trackers.forEach { it.setUserProperty(property) }
            }
        }
    }
}
