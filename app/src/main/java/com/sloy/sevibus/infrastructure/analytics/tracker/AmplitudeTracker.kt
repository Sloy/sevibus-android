package com.sloy.sevibus.infrastructure.analytics.tracker

import android.content.Context
import com.amplitude.android.Amplitude
import com.amplitude.android.AutocaptureOption
import com.amplitude.android.Configuration
import com.amplitude.android.ExperimentalAmplitudeFeature
import com.amplitude.core.ServerZone
import com.sloy.sevibus.infrastructure.BuildVariant
import com.sloy.sevibus.infrastructure.analytics.AnalyticsSettingsDataSource
import com.sloy.sevibus.infrastructure.analytics.SevEvent
import com.sloy.sevibus.infrastructure.analytics.Tracker
import com.sloy.sevibus.infrastructure.session.SessionService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class AmplitudeTracker(
    private val context: Context,
    private val analyticsSettingsDataSource: AnalyticsSettingsDataSource,
    private val sessionService: SessionService,
) : Tracker {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val amplitude = CompletableDeferred<Amplitude>()

    init {
        initSdk()
        monitorOptOut()
        monitorUserSession()
    }

    private fun initSdk() {
        scope.launch {
            val enabled = analyticsSettingsDataSource.isAnalyticsEnabled()
            amplitude.complete(createAmplitudeInstance(context, optOut = !enabled))
        }
    }

    private fun monitorOptOut() {
        analyticsSettingsDataSource.observeAnalyticsEnabled()
            .distinctUntilChanged()
            .onEach { enabled ->
                amplitude.await().configuration.optOut = !enabled
            }
            .launchIn(scope)
    }

    private fun monitorUserSession() {
        sessionService.observeCurrentUser()
            .distinctUntilChanged()
            .onEach { user ->
                if (user != null) {
                    amplitude.await().setUserId(user.id)
                } else if (amplitude.await().getUserId() != null) {
                    amplitude.await().reset()
                }
            }
            .launchIn(scope)
    }

    @OptIn(ExperimentalAmplitudeFeature::class)
    private fun createAmplitudeInstance(
        context: Context,
        optOut: Boolean
    ): Amplitude = Amplitude(
        Configuration(
            context = context,
            apiKey = if (BuildVariant.isRelease()) PROD_PUBLIC_KEY else DEV_PUBLIC_KEY,
            autocapture = setOf(
                AutocaptureOption.SESSIONS,
                AutocaptureOption.APP_LIFECYCLES,
                AutocaptureOption.DEEP_LINKS,
                AutocaptureOption.FRUSTRATION_INTERACTIONS,
            ),
            flushIntervalMillis = if (BuildVariant.isRelease()) PROD_FLUSH_INTERVAL else DEV_FLUSH_INTERVAL,
            identifyBatchIntervalMillis = (if (BuildVariant.isRelease()) PROD_FLUSH_INTERVAL else DEV_FLUSH_INTERVAL).toLong(),
            serverZone = ServerZone.EU,
            optOut = optOut,
        )
    )

    override fun track(event: SevEvent) {
        scope.launch {
            amplitude.await().track(event.name, event.properties.toMap())
        }
    }

    private companion object {
        const val DEV_PUBLIC_KEY = "d80b706ae22f1b16240ea9321eaed101"
        const val PROD_PUBLIC_KEY = "314a40d0364157fd867f887e44327af4"
        const val DEV_FLUSH_INTERVAL = 5_000
        const val PROD_FLUSH_INTERVAL = 30_000

    }
}