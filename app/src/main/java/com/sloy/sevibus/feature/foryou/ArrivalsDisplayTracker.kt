package com.sloy.sevibus.feature.foryou

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.LifecycleStartEffect
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.infrastructure.analytics.SevEvent
import com.sloy.sevibus.infrastructure.analytics.events.Events

/**
 * Decides when the arrivals of every stop in a list have been displayed, once per screen view.
 */
class ArrivalsDisplayTracker(private val clock: () -> Long) {

    private val arrivalsCountByStop = mutableMapOf<StopId, Int>()
    private var stopIds: List<StopId> = emptyList()
    private var viewStartedAt: Long? = null
    private var isReported = false

    fun onViewStarted(): Displayed? {
        viewStartedAt = clock()
        isReported = false
        return reportIfAllLoaded()
    }

    fun onViewStopped() {
        viewStartedAt = null
    }

    fun onStopsChanged(stopIds: List<StopId>): Displayed? {
        this.stopIds = stopIds
        return reportIfAllLoaded()
    }

    /**
     * @param arrivalsCount null while the arrivals of the stop are loading
     */
    fun onArrivalsChanged(stopId: StopId, arrivalsCount: Int?): Displayed? {
        if (arrivalsCount == null) arrivalsCountByStop.remove(stopId) else arrivalsCountByStop[stopId] = arrivalsCount
        return reportIfAllLoaded()
    }

    private fun reportIfAllLoaded(): Displayed? {
        val startedAt = viewStartedAt ?: return null
        if (isReported || stopIds.isEmpty() || !stopIds.all { it in arrivalsCountByStop }) return null
        isReported = true
        return Displayed(
            stopCount = stopIds.size,
            arrivalsCount = stopIds.sumOf { arrivalsCountByStop.getValue(it) },
            latencyMs = clock() - startedAt,
        )
    }

    data class Displayed(val stopCount: Int, val arrivalsCount: Int, val latencyMs: Long)
}

/**
 * Tracks Arrivals Displayed when every stop in [stopIds] has loaded its arrivals while the screen is shown.
 *
 * @return the callback each list item calls when its arrivals change, with null while loading
 */
@Composable
fun rememberArrivalsDisplayReporter(
    screen: Events.ArrivalsScreen,
    stopIds: List<StopId>,
    isShown: Boolean,
    onTrack: (SevEvent) -> Unit,
): (StopId, Int?) -> Unit {
    val currentOnTrack by rememberUpdatedState(onTrack)
    val tracker = remember { ArrivalsDisplayTracker(SystemClock::elapsedRealtime) }
    val report: (ArrivalsDisplayTracker.Displayed?) -> Unit = remember {
        { displayed ->
            displayed?.let { currentOnTrack(Events.ArrivalsDisplayed(screen, null, it.stopCount, it.arrivalsCount, it.latencyMs)) }
        }
    }
    LaunchedEffect(stopIds) {
        report(tracker.onStopsChanged(stopIds))
    }
    if (isShown) {
        LifecycleStartEffect(tracker) {
            report(tracker.onViewStarted())
            onStopOrDispose { tracker.onViewStopped() }
        }
    }
    return remember { { stopId, arrivalsCount -> report(tracker.onArrivalsChanged(stopId, arrivalsCount)) } }
}
