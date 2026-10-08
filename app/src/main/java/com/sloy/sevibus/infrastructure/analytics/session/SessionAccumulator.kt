package com.sloy.sevibus.infrastructure.analytics.session

import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.infrastructure.analytics.SevEvent
import com.sloy.sevibus.infrastructure.analytics.events.Clicks
import com.sloy.sevibus.infrastructure.analytics.events.Events
import com.sloy.sevibus.infrastructure.analytics.events.Events.SessionSummary.Screen as SessionSummaryScreen
import com.sloy.sevibus.infrastructure.analytics.events.Screens
import com.sloy.sevibus.navigation.NavigationDestination

/**
 * Collects what happens during one foreground period. Not thread safe, [SessionTracker] synchronizes access.
 */
class SessionAccumulator(private val startedAt: Long) {

    private var stopViews = 0
    private var arrivalsViews = 0
    private val stopsLastViewedAt = mutableMapOf<StopId, Long>()
    private val entrySources = linkedSetOf<String>()
    private val featuresUsed = linkedSetOf<Feature>()
    private var visitedCardsScreen = false
    private var visitedOtherScreen = false
    private var usedExplorerFeature = false
    private var stopStayStartedAt: Long? = null
    private var longestStopStayMillis = 0L
    private var mapExplored = false

    fun onInitialDestination(destination: NavigationDestination, now: Long) {
        onScreenShown(destination.toScreen(), now)
        if (destination is NavigationDestination.StopDetail) {
            stopStayStartedAt = now
        }
    }

    fun onEvent(event: SevEvent, now: Long) {
        event.toScreen()?.let { screen -> onScreenShown(screen, now) }
        when (event) {
            is Screens.StopDetailsViewed -> {
                stopViews++
                entrySources += event.source
                stopsLastViewedAt[event.stopId] = now
                stopStayStartedAt = now
            }

            is Screens.LinesViewed, is Screens.LineStopsViewed, is Screens.SearchViewed -> usedExplorerFeature = true
            is Clicks.MapStopClicked -> {
                usedExplorerFeature = true
                featuresUsed += Feature.MAP
            }

            is Events.MapExplored -> featuresUsed += Feature.MAP
            is Events.ArrivalsDisplayed -> {
                arrivalsViews++
                when (event.screen) {
                    Events.ArrivalsScreen.FAVORITES -> featuresUsed += Feature.FAVORITES
                    Events.ArrivalsScreen.NEARBY -> featuresUsed += Feature.NEARBY
                    Events.ArrivalsScreen.STOP_DETAIL -> {}
                }
            }

            is Clicks.FavoriteStopClicked, is Clicks.AddFavoriteClicked, is Clicks.RemoveFavoriteClicked -> featuresUsed += Feature.FAVORITES
            is Clicks.NearbyStopClicked -> featuresUsed += Feature.NEARBY
            else -> {}
        }
    }

    /**
     * @return true only the first time it's called in this session
     */
    fun markMapExplored(): Boolean {
        if (mapExplored) return false
        mapExplored = true
        return true
    }

    fun finish(now: Long, lastDestination: NavigationDestination): SessionStats {
        closeStopStay(now)
        return SessionStats(
            durationMillis = now - startedAt,
            lastScreen = lastDestination.toSummaryScreen(),
            stopViews = stopViews,
            stopsLastViewedAt = stopsLastViewedAt.toMap(),
            arrivalsViews = arrivalsViews,
            entrySources = entrySources.toList(),
            featuresUsed = featuresUsed.toList(),
            onlyCardsScreens = visitedCardsScreen && !visitedOtherScreen,
            usedExplorerFeature = usedExplorerFeature,
            longestStopStayMillis = longestStopStayMillis,
        )
    }

    private fun onScreenShown(screen: Screen, now: Long) {
        closeStopStay(now)
        if (screen.isCards) visitedCardsScreen = true else visitedOtherScreen = true
        screen.feature?.let { featuresUsed += it }
    }

    private fun closeStopStay(now: Long) {
        stopStayStartedAt?.let { startedAt ->
            longestStopStayMillis = maxOf(longestStopStayMillis, now - startedAt)
        }
        stopStayStartedAt = null
    }

    private enum class Screen(val isCards: Boolean, val feature: Feature?) {
        FOR_YOU(false, null),
        LINES(false, Feature.LINES),
        STOP_DETAIL(false, null),
        CARDS(true, Feature.CARDS),
        EDIT_FAVORITES(false, Feature.FAVORITES),
        SEARCH(false, Feature.SEARCH),
        SETTINGS(false, Feature.SETTINGS),
    }

    private fun SevEvent.toScreen(): Screen? = when (this) {
        is Screens.ForYouViewed -> Screen.FOR_YOU
        is Screens.LinesViewed, is Screens.LineStopsViewed -> Screen.LINES
        is Screens.StopDetailsViewed -> Screen.STOP_DETAIL
        is Screens.CardsViewed, is Screens.CardsHelpViewed -> Screen.CARDS
        is Screens.EditFavoritesViewed -> Screen.EDIT_FAVORITES
        is Screens.SearchViewed -> Screen.SEARCH
        is Screens.SettingsViewed -> Screen.SETTINGS
        else -> null
    }

    private fun NavigationDestination.toScreen(): Screen = when (this) {
        is NavigationDestination.ForYou -> Screen.FOR_YOU
        is NavigationDestination.Lines, is NavigationDestination.LineStops -> Screen.LINES
        is NavigationDestination.StopDetail -> Screen.STOP_DETAIL
        is NavigationDestination.Cards, is NavigationDestination.CardsHelp -> Screen.CARDS
        is NavigationDestination.EditFavorites -> Screen.EDIT_FAVORITES
        is NavigationDestination.Search -> Screen.SEARCH
        is NavigationDestination.Settings -> Screen.SETTINGS
        else -> Screen.FOR_YOU
    }
}

private fun NavigationDestination.toSummaryScreen(): SessionSummaryScreen = when (this) {
    is NavigationDestination.ForYou -> SessionSummaryScreen.FOR_YOU
    is NavigationDestination.Lines -> SessionSummaryScreen.LINES
    is NavigationDestination.LineStops -> SessionSummaryScreen.LINE_STOPS
    is NavigationDestination.StopDetail -> SessionSummaryScreen.STOP_DETAIL
    is NavigationDestination.Cards -> SessionSummaryScreen.CARDS
    is NavigationDestination.CardsHelp -> SessionSummaryScreen.CARDS_HELP
    is NavigationDestination.EditFavorites -> SessionSummaryScreen.EDIT_FAVORITES
    is NavigationDestination.Search -> SessionSummaryScreen.SEARCH
    is NavigationDestination.Settings -> SessionSummaryScreen.SETTINGS
    else -> error("No session summary screen for destination $this")
}

enum class Feature { FAVORITES, NEARBY, MAP, LINES, SEARCH, CARDS, SETTINGS }

data class SessionStats(
    val durationMillis: Long,
    val lastScreen: SessionSummaryScreen,
    val stopViews: Int,
    val stopsLastViewedAt: Map<StopId, Long>,
    val arrivalsViews: Int,
    val entrySources: List<String>,
    val featuresUsed: List<Feature>,
    val onlyCardsScreens: Boolean,
    val usedExplorerFeature: Boolean,
    val longestStopStayMillis: Long,
)
