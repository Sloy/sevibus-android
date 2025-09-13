package com.sloy.sevibus.infrastructure.analytics.tracker

import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.ktx.Firebase
import com.sloy.sevibus.infrastructure.analytics.AnalyticsSettingsDataSource
import com.sloy.sevibus.infrastructure.analytics.SevEvent
import com.sloy.sevibus.infrastructure.analytics.Tracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class FirebaseTracker(
    analyticsSettingsDataSource: AnalyticsSettingsDataSource
) : Tracker {

    private val firebaseAnalytics = Firebase.analytics
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        analyticsSettingsDataSource.observeAnalyticsEnabled()
            .distinctUntilChanged()
            .onEach { enabled ->
                firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
            }
            .launchIn(scope)
    }

    override fun track(event: SevEvent) {
        if (event.properties.isEmpty()) {
            firebaseAnalytics.logEvent(event.name.toUnderscores(), null)
        } else {
            firebaseAnalytics.logEvent(event.name.toUnderscores()) {
                event.properties.forEach { (key, value) ->
                    val formattedKey = key.toUnderscores()
                    when (value) {
                        is String -> param(formattedKey, value)
                        is Int -> param(formattedKey, value.toLong())
                        is Long -> param(formattedKey, value)
                        is Double -> param(formattedKey, value)
                        is Float -> param(formattedKey, value.toDouble())
                        null -> {}
                        else -> param(formattedKey, value.toString()) // Fallback
                    }
                }
            }
        }
    }

}

private fun String.toUnderscores() = this.replace(" ", "_")