package com.sloy.sevibus.infrastructure.analytics.session

import com.sloy.sevibus.infrastructure.analytics.events.Events.SessionSummary.SessionType
import com.sloy.sevibus.infrastructure.analytics.events.UserProperty.UsageProfile.Profile
import java.time.LocalDate
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object SessionClassifier {

    val WAITER_STOP_STAY = 180.seconds
    val WAITER_REPEATED_STOP_WINDOW = 20.minutes
    val GLANCER_MAX_DURATION = 60.seconds
    const val USAGE_HISTORY_DAYS = 30L
    const val COMMUTER_WINDOW_DAYS = 7L
    const val COMMUTER_MIN_ACTIVE_DAYS = 3

    /**
     * @param previousStopViews stops viewed in previous sessions, with the time they were last viewed
     */
    fun classify(stats: SessionStats, previousStopViews: List<StopView>): SessionType = when {
        stats.onlyCardsScreens && stats.stopViews == 0 -> SessionType.CARD_CHECKER
        stats.longestStopStayMillis > WAITER_STOP_STAY.inWholeMilliseconds -> SessionType.WAITER
        stats.viewsStopAgainWithin(previousStopViews) -> SessionType.WAITER
        stats.usedExplorerFeature -> SessionType.EXPLORER
        stats.durationMillis < GLANCER_MAX_DURATION.inWholeMilliseconds && stats.arrivalsViews > 0 -> SessionType.GLANCER
        else -> SessionType.OTHER
    }

    /**
     * Most frequent session type in the history, or [Profile.MIXED] if none is above 50%.
     * Sessions classified as [SessionType.OTHER] have no profile, so they are left out.
     */
    fun usageProfile(history: List<SessionRecord>, today: LocalDate): Profile? {
        val since = today.minusDays(USAGE_HISTORY_DAYS - 1).toEpochDay()
        val types = history
            .filter { it.epochDay >= since }
            .mapNotNull { it.sessionType.toProfile() }
        if (types.isEmpty()) return null
        val (mostFrequent, count) = types.groupingBy { it }.eachCount().maxBy { it.value }
        return if (count * 2 > types.size) mostFrequent else Profile.MIXED
    }

    fun isCommuter(history: List<SessionRecord>, today: LocalDate, favoritesCount: Int): Boolean {
        val since = today.minusDays(COMMUTER_WINDOW_DAYS - 1).toEpochDay()
        val activeDays = history.filter { it.epochDay >= since }.map { it.epochDay }.distinct().size
        return activeDays >= COMMUTER_MIN_ACTIVE_DAYS && favoritesCount >= 1
    }

    private fun SessionStats.viewsStopAgainWithin(previousStopViews: List<StopView>): Boolean {
        val window = WAITER_REPEATED_STOP_WINDOW.inWholeMilliseconds
        return stopsLastViewedAt.any { (stopId, viewedAt) ->
            previousStopViews.any { it.stopId == stopId && viewedAt - it.viewedAt in 0..window }
        }
    }

    private fun SessionType.toProfile(): Profile? = when (this) {
        SessionType.GLANCER -> Profile.GLANCER
        SessionType.WAITER -> Profile.WAITER
        SessionType.EXPLORER -> Profile.EXPLORER
        SessionType.CARD_CHECKER -> Profile.CARD_CHECKER
        SessionType.OTHER -> null
    }
}
