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
import kotlinx.coroutines.runBlocking

class AmplitudeTracker(
    context: Context,
    analyticsSettingsDataSource: AnalyticsSettingsDataSource
) : Tracker {

    @OptIn(ExperimentalAmplitudeFeature::class)
    private val amplitude = Amplitude(
        Configuration(
            context = context,
            apiKey = if (BuildVariant.isRelease()) PROD_PUBIC_KEY else DEV_PUBIC_KEY,
            autocapture = setOf(
                AutocaptureOption.SESSIONS,
                AutocaptureOption.APP_LIFECYCLES,
                AutocaptureOption.DEEP_LINKS,
                AutocaptureOption.FRUSTRATION_INTERACTIONS,
            ),
            flushIntervalMillis = if (BuildVariant.isRelease()) PROD_FLUSH_INTERVAL else DEV_FLUSH_INTERVAL,
            identifyBatchIntervalMillis = (if (BuildVariant.isRelease()) PROD_FLUSH_INTERVAL else DEV_FLUSH_INTERVAL).toLong(),
            serverZone = ServerZone.EU,
            optOut = runBlocking { !analyticsSettingsDataSource.isAnalyticsEnabled() }
        )
    )

    override fun track(event: SevEvent) {
        amplitude.track(event.name)
    }

    private companion object {
        const val DEV_PUBIC_KEY = "3850b1abe822f3a889751f845f401b15"
        const val PROD_PUBIC_KEY = ""
        const val DEV_FLUSH_INTERVAL = 5_000
        const val PROD_FLUSH_INTERVAL = 30_000

    }
}