package com.sloy.sevibus.infrastructure.analytics.session

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.sloy.sevibus.domain.repository.FavoriteRepository
import com.sloy.sevibus.infrastructure.SevLogger
import com.sloy.sevibus.infrastructure.analytics.Analytics
import com.sloy.sevibus.infrastructure.analytics.SevEvent
import com.sloy.sevibus.infrastructure.analytics.Tracker
import com.sloy.sevibus.infrastructure.analytics.analyticsValue
import com.sloy.sevibus.infrastructure.analytics.events.Events
import com.sloy.sevibus.infrastructure.analytics.events.UserProperty
import com.sloy.sevibus.navigation.SevNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import java.time.LocalDate

/**
 * Sends a Session Summary every time the app goes to background, and updates the usage profile user properties.
 *
 * Receives [Analytics] lazily because it's also one of its trackers.
 */
class SessionTracker(
    private val analytics: Lazy<Analytics>,
    private val navigator: SevNavigator,
    private val favoriteRepository: FavoriteRepository,
    private val historyDataSource: SessionHistoryDataSource,
    private val clock: Clock = Clock.systemDefaultZone(),
) : Tracker, DefaultLifecycleObserver {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val lock = Any()
    private val historyMutex = Mutex()
    private var session: SessionAccumulator? = null

    fun start() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        synchronized(lock) {
            session = SessionAccumulator(startedAt = clock.millis()).apply {
                onInitialDestination(navigator.destination.value, clock.millis())
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        val stats = synchronized(lock) {
            val stats = session?.finish(clock.millis(), lastDestination = navigator.destination.value)
            session = null
            stats
        } ?: return
        scope.launch {
            runCatching { report(stats) }
                .onFailure { SevLogger.logE(it, "Error reporting session summary") }
        }
    }

    override fun track(event: SevEvent) {
        synchronized(lock) {
            session?.onEvent(event, clock.millis())
        }
    }

    /**
     * @return true the first time the map is explored in the current session
     */
    fun markMapExplored(): Boolean = synchronized(lock) {
        session?.markMapExplored() ?: false
    }

    private suspend fun report(stats: SessionStats) = historyMutex.withLock {
        val history = historyDataSource.obtainHistory()
        val today = LocalDate.now(clock)
        val sessionType = SessionClassifier.classify(stats, history.recentStopViews)
        analytics.value.track(stats.toSummary(sessionType))

        val updatedHistory = history.record(stats, sessionType, today)
        historyDataSource.saveHistory(updatedHistory)

        SessionClassifier.usageProfile(updatedHistory.sessions, today)?.let { profile ->
            analytics.value.setUserProperty(UserProperty.UsageProfile(profile))
        }
        val favoritesCount = favoriteRepository.observeFavorites().first().size
        analytics.value.setUserProperty(UserProperty.IsCommuter(SessionClassifier.isCommuter(updatedHistory.sessions, today, favoritesCount)))
    }

    private fun SessionHistory.record(stats: SessionStats, sessionType: Events.SessionSummary.SessionType, today: LocalDate): SessionHistory {
        val oldestDay = today.minusDays(SessionClassifier.USAGE_HISTORY_DAYS - 1).toEpochDay()
        val oldestStopView = clock.millis() - SessionClassifier.WAITER_REPEATED_STOP_WINDOW.inWholeMilliseconds
        val sessionStopViews = stats.stopsLastViewedAt.map { (stopId, viewedAt) -> StopView(stopId, viewedAt) }
        return SessionHistory(
            sessions = (sessions + SessionRecord(today.toEpochDay(), sessionType)).filter { it.epochDay >= oldestDay },
            recentStopViews = (recentStopViews + sessionStopViews).filter { it.viewedAt >= oldestStopView },
        )
    }

    private fun SessionStats.toSummary(sessionType: Events.SessionSummary.SessionType) = Events.SessionSummary(
        sessionType = sessionType,
        durationSeconds = durationMillis / 1000,
        stopViews = stopViews,
        distinctStops = stopsLastViewedAt.size,
        arrivalsViews = arrivalsViews,
        entrySources = entrySources,
        featuresUsed = featuresUsed.map { it.analyticsValue },
        lastScreen = lastScreen,
    )
}
