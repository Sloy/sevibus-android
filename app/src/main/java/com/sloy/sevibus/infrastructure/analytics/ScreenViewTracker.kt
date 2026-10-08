package com.sloy.sevibus.infrastructure.analytics

import com.sloy.sevibus.infrastructure.analytics.events.toScreenViewEvent
import com.sloy.sevibus.navigation.SevNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach

/**
 * Tracks screen views for the whole process, so recreating the activity doesn't track the current screen again.
 */
class ScreenViewTracker(
    private val navigator: SevNavigator,
    private val analytics: Analytics,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun start() {
        navigator.transitions
            .mapNotNull { it.toScreenViewEvent() }
            .onEach { analytics.track(it) }
            .launchIn(scope)
    }
}
