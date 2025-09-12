package com.sloy.sevibus.infrastructure.analytics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class Analytics(
    private val trackers: List<Tracker>,
    private val analyticsSettingsDataSource: AnalyticsSettingsDataSource
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun track(event: SevEvent) {
        scope.launch {
            if (analyticsSettingsDataSource.isAnalyticsEnabled()) {
                trackers.forEach { it.track(event) }
            }
        }
    }
}