package com.sloy.sevibus.infrastructure.analytics.events

import com.sloy.sevibus.domain.model.LineId
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.infrastructure.analytics.Analytics
import com.sloy.sevibus.infrastructure.analytics.SevEvent
import com.sloy.sevibus.navigation.NavigationDestination

fun Analytics.track(destination: NavigationDestination) {
    val event = when (destination) {
        is NavigationDestination.ForYou -> Screens.ForYouViewed
        is NavigationDestination.Lines -> Screens.LinesViewed
        is NavigationDestination.Cards -> Screens.CardsViewed
        is NavigationDestination.CardsHelp -> Screens.CardsHelpViewed
        is NavigationDestination.LineStops -> Screens.LineStopsViewed(destination.lineId)
        is NavigationDestination.StopDetail -> Screens.StopDetailsViewed(destination.stopId)
        is NavigationDestination.EditFavorites -> Screens.EditFavoritesViewed
        is NavigationDestination.Search -> Screens.SearchViewed
        is NavigationDestination.Settings -> Screens.SettingsViewed
        else -> error("No tracking event for destination $destination")
    }
    track(event)
}

interface Screens {
    object ForYouViewed : SevEvent(
        "For You Viewed"
    )

    object LinesViewed : SevEvent(
        "Lines Viewed"
    )

    object CardsViewed : SevEvent(
        "Cards Viewed"
    )

    object CardsHelpViewed : SevEvent(
        "Cards Help Viewed"
    )

    class LineStopsViewed(val lineId: LineId) : SevEvent(
        "Line Stops Viewed",
        "lineId" to lineId
    )

    class StopDetailsViewed(val stopId: StopId) : SevEvent(
        "Stop Details Viewed",
        "stopId" to stopId
    )

    object EditFavoritesViewed : SevEvent(
        "Edit Favorites Viewed"
    )

    object SearchViewed : SevEvent(
        "Search Viewed"
    )

    object SettingsViewed : SevEvent(
        "Settings Viewed"
    )
}
