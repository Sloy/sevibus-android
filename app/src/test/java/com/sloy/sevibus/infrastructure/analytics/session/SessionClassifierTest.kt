package com.sloy.sevibus.infrastructure.analytics.session

import com.sloy.sevibus.infrastructure.analytics.events.Clicks
import com.sloy.sevibus.infrastructure.analytics.events.Events
import com.sloy.sevibus.infrastructure.analytics.events.Events.ArrivalsScreen
import com.sloy.sevibus.infrastructure.analytics.events.Events.SessionSummary.SessionType
import com.sloy.sevibus.infrastructure.analytics.events.Screens
import com.sloy.sevibus.infrastructure.analytics.events.UserProperty.UsageProfile.Profile
import com.sloy.sevibus.navigation.NavigationDestination
import com.sloy.sevibus.navigation.NavigationTrigger
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNull
import strikt.assertions.isTrue
import java.time.LocalDate
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class SessionClassifierTest {

    private var now = SESSION_START

    @Test
    fun `card checker when only cards screens are visited`() {
        val session = session(initialDestination = NavigationDestination.Cards()) {
            event(Screens.CardsHelpViewed(NavigationTrigger.NAVIGATION))
            event(Screens.CardsViewed(NavigationTrigger.BACK))
        }

        expectThat(classify(session)).isEqualTo(SessionType.CARD_CHECKER)
    }

    @Test
    fun `not a card checker when another screen is visited`() {
        val session = session(initialDestination = NavigationDestination.ForYou) {
            event(Screens.CardsViewed(NavigationTrigger.NAVIGATION))
        }

        expectThat(classify(session)).isEqualTo(SessionType.OTHER)
    }

    @Test
    fun `waiter when a single stop details stay is longer than 3 minutes`() {
        val session = session {
            event(stopDetailsViewed(STOP_ID))
            wait(181.seconds)
            event(Screens.ForYouViewed(NavigationTrigger.BACK))
        }

        expectThat(classify(session)).isEqualTo(SessionType.WAITER)
    }

    @Test
    fun `waiter when the app goes to background after a long stay in stop details`() {
        val session = session {
            event(stopDetailsViewed(STOP_ID))
            event(Clicks.MapStopClicked(STOP_ID))
            wait(4.minutes)
        }

        expectThat(classify(session)).isEqualTo(SessionType.WAITER)
    }

    @Test
    fun `not a waiter when the stop details stays are short`() {
        val session = session {
            event(stopDetailsViewed(STOP_ID))
            wait(2.minutes)
            event(stopDetailsViewed(OTHER_STOP_ID))
            wait(2.minutes)
        }

        expectThat(classify(session)).isEqualTo(SessionType.OTHER)
    }

    @Test
    fun `waiter when the same stop was viewed in a previous session less than 20 minutes ago`() {
        val session = session {
            event(stopDetailsViewed(STOP_ID))
            wait(10.seconds)
        }
        val previousStopViews = listOf(StopView(STOP_ID, SESSION_START - 15.minutes.inWholeMilliseconds))

        expectThat(SessionClassifier.classify(session, previousStopViews)).isEqualTo(SessionType.WAITER)
    }

    @Test
    fun `not a waiter when the same stop was viewed more than 20 minutes ago`() {
        val session = session {
            event(stopDetailsViewed(STOP_ID))
            wait(10.seconds)
        }
        val previousStopViews = listOf(StopView(STOP_ID, SESSION_START - 25.minutes.inWholeMilliseconds))

        expectThat(SessionClassifier.classify(session, previousStopViews)).isEqualTo(SessionType.OTHER)
    }

    @Test
    fun `explorer when stops are tapped on the map`() {
        val session = session { event(Clicks.MapStopClicked(STOP_ID)) }

        expectThat(classify(session)).isEqualTo(SessionType.EXPLORER)
    }

    @Test
    fun `explorer when lines are visited`() {
        val session = session { event(Screens.LinesViewed(NavigationTrigger.NAVIGATION)) }

        expectThat(classify(session)).isEqualTo(SessionType.EXPLORER)
    }

    @Test
    fun `explorer when search is used`() {
        val session = session { event(Screens.SearchViewed(NavigationTrigger.NAVIGATION)) }

        expectThat(classify(session)).isEqualTo(SessionType.EXPLORER)
    }

    @Test
    fun `glancer when arrivals are seen in a short session`() {
        val session = session {
            event(arrivalsDisplayed(ArrivalsScreen.FAVORITES))
            wait(20.seconds)
        }

        expectThat(classify(session)).isEqualTo(SessionType.GLANCER)
    }

    @Test
    fun `not a glancer when the session is longer than a minute`() {
        val session = session {
            event(arrivalsDisplayed(ArrivalsScreen.FAVORITES))
            wait(61.seconds)
        }

        expectThat(classify(session)).isEqualTo(SessionType.OTHER)
    }

    @Test
    fun `other when no arrivals are seen`() {
        val session = session {
            event(Screens.SettingsViewed(NavigationTrigger.NAVIGATION))
            wait(5.seconds)
        }

        expectThat(classify(session)).isEqualTo(SessionType.OTHER)
    }

    @Test
    fun `waiter takes precedence over explorer and explorer over glancer`() {
        val waiterAndExplorer = session {
            event(Screens.SearchViewed(NavigationTrigger.NAVIGATION))
            event(stopDetailsViewed(STOP_ID))
            wait(4.minutes)
        }
        val explorerAndGlancer = session {
            event(Screens.LinesViewed(NavigationTrigger.NAVIGATION))
            event(arrivalsDisplayed(ArrivalsScreen.STOP_DETAIL))
        }

        expectThat(classify(waiterAndExplorer)).isEqualTo(SessionType.WAITER)
        expectThat(classify(explorerAndGlancer)).isEqualTo(SessionType.EXPLORER)
    }

    @Test
    fun `session stats count stop views, sources and features`() {
        val session = session {
            event(arrivalsDisplayed(ArrivalsScreen.FAVORITES))
            event(stopDetailsViewed(STOP_ID, source = "favorites"))
            event(Screens.ForYouViewed(NavigationTrigger.BACK))
            event(stopDetailsViewed(STOP_ID, source = "nearby"))
            event(Screens.ForYouViewed(NavigationTrigger.BACK))
            event(stopDetailsViewed(OTHER_STOP_ID, source = "map"))
            event(Events.MapExplored)
            event(Screens.SettingsViewed(NavigationTrigger.NAVIGATION))
            wait(30.seconds)
        }

        expectThat(session) {
            get { durationMillis }.isEqualTo(30.seconds.inWholeMilliseconds)
            get { stopViews }.isEqualTo(3)
            get { stopsLastViewedAt.keys }.isEqualTo(setOf(STOP_ID, OTHER_STOP_ID))
            get { arrivalsViews }.isEqualTo(1)
            get { entrySources }.isEqualTo(listOf("favorites", "nearby", "map"))
            get { featuresUsed }.isEqualTo(listOf(Feature.FAVORITES, Feature.MAP, Feature.SETTINGS))
        }
    }

    @Test
    fun `last screen is the destination shown when the app goes to background`() {
        val glancedAtFavorites = session { event(arrivalsDisplayed(ArrivalsScreen.FAVORITES)) }
        val leftOnStopDetails = session(lastDestination = NavigationDestination.StopDetail(STOP_ID)) {
            event(stopDetailsViewed(STOP_ID))
        }
        val leftOnCardsHelp = session(lastDestination = NavigationDestination.CardsHelp) {}

        expectThat(glancedAtFavorites.lastScreen).isEqualTo(Events.SessionSummary.Screen.FOR_YOU)
        expectThat(leftOnStopDetails.lastScreen).isEqualTo(Events.SessionSummary.Screen.STOP_DETAIL)
        expectThat(leftOnCardsHelp.lastScreen).isEqualTo(Events.SessionSummary.Screen.CARDS_HELP)
    }

    @Test
    fun `usage profile is the most frequent session type`() {
        val history = records(SessionType.GLANCER, SessionType.GLANCER, SessionType.WAITER)

        expectThat(SessionClassifier.usageProfile(history, TODAY)).isEqualTo(Profile.GLANCER)
    }

    @Test
    fun `usage profile is mixed when no session type is above half`() {
        val history = records(SessionType.GLANCER, SessionType.WAITER, SessionType.EXPLORER, SessionType.GLANCER)

        expectThat(SessionClassifier.usageProfile(history, TODAY)).isEqualTo(Profile.MIXED)
    }

    @Test
    fun `usage profile ignores other sessions and sessions older than 30 days`() {
        val history = records(SessionType.OTHER, SessionType.OTHER, SessionType.WAITER) +
                SessionRecord(TODAY.minusDays(30).toEpochDay(), SessionType.EXPLORER)

        expectThat(SessionClassifier.usageProfile(history, TODAY)).isEqualTo(Profile.WAITER)
    }

    @Test
    fun `no usage profile without classified sessions`() {
        expectThat(SessionClassifier.usageProfile(records(SessionType.OTHER), TODAY)).isNull()
    }

    @Test
    fun `commuter when active 3 different days in the last week with favorites`() {
        val history = listOf(0L, 2L, 6L, 6L).map { SessionRecord(TODAY.minusDays(it).toEpochDay(), SessionType.GLANCER) }

        expectThat(SessionClassifier.isCommuter(history, TODAY, favoritesCount = 1)).isTrue()
        expectThat(SessionClassifier.isCommuter(history, TODAY, favoritesCount = 0)).isFalse()
    }

    @Test
    fun `not a commuter when the active days are older than a week`() {
        val history = listOf(0L, 2L, 7L).map { SessionRecord(TODAY.minusDays(it).toEpochDay(), SessionType.GLANCER) }

        expectThat(SessionClassifier.isCommuter(history, TODAY, favoritesCount = 2)).isFalse()
    }

    private fun session(
        initialDestination: NavigationDestination = NavigationDestination.ForYou,
        lastDestination: NavigationDestination = initialDestination,
        actions: SessionAccumulator.() -> Unit,
    ): SessionStats {
        now = SESSION_START
        val accumulator = SessionAccumulator(startedAt = now)
        accumulator.onInitialDestination(initialDestination, now)
        accumulator.actions()
        return accumulator.finish(now, lastDestination)
    }

    private fun SessionAccumulator.event(event: com.sloy.sevibus.infrastructure.analytics.SevEvent) = onEvent(event, now)

    private fun wait(duration: Duration) {
        now += duration.inWholeMilliseconds
    }

    private fun classify(stats: SessionStats) = SessionClassifier.classify(stats, previousStopViews = emptyList())

    private fun stopDetailsViewed(stopId: Int, source: String = "other") =
        Screens.StopDetailsViewed(stopId, source, highlightedLineId = null, trigger = NavigationTrigger.NAVIGATION)

    private fun arrivalsDisplayed(screen: ArrivalsScreen) =
        Events.ArrivalsDisplayed(screen, stopId = null, stopCount = 1, arrivalsCount = 3, latencyMs = 200)

    private fun records(vararg types: SessionType) = types.map { SessionRecord(TODAY.toEpochDay(), it) }
}

private const val STOP_ID = 11
private const val OTHER_STOP_ID = 12
private const val SESSION_START = 1_700_000_000_000L
private val TODAY = LocalDate.of(2026, 10, 8)
