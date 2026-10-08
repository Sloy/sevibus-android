package com.sloy.sevibus.infrastructure.analytics.events

import com.sloy.sevibus.domain.model.LineId
import com.sloy.sevibus.domain.model.RouteId
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.infrastructure.analytics.SevEvent
import com.sloy.sevibus.infrastructure.analytics.analyticsValue
import com.sloy.sevibus.navigation.NavigationDestination
import com.sloy.sevibus.navigation.NavigationTransition
import com.sloy.sevibus.navigation.NavigationTrigger

fun NavigationTransition.toScreenViewEvent(): SevEvent? {
    return when (destination) {
        is NavigationDestination.ForYou -> Screens.ForYouViewed(trigger)
        is NavigationDestination.Lines -> Screens.LinesViewed(trigger)
        is NavigationDestination.Cards -> Screens.CardsViewed(trigger)
        is NavigationDestination.CardsHelp -> Screens.CardsHelpViewed(trigger)
        is NavigationDestination.LineStops -> {
            val isSameLine = previous is NavigationDestination.LineStops && previous.lineId == destination.lineId
            if (isSameLine) null else Screens.LineStopsViewed(destination.lineId, destination.routeId, trigger)
        }

        is NavigationDestination.StopDetail -> Screens.StopDetailsViewed(
            stopId = destination.stopId,
            source = if (trigger == NavigationTrigger.BACK) Screens.StopDetailsViewed.SOURCE_BACK else destination.source.analyticsValue,
            highlightedLineId = destination.highlightedLine,
            trigger = trigger,
        )

        is NavigationDestination.EditFavorites -> Screens.EditFavoritesViewed(trigger)
        is NavigationDestination.Search -> Screens.SearchViewed(trigger)
        is NavigationDestination.Settings -> Screens.SettingsViewed(trigger)
        else -> error("No tracking event for destination $destination")
    }
}

interface Screens {
    data class ForYouViewed(val trigger: NavigationTrigger) : SevEvent(
        "For You Viewed",
        "trigger" to trigger.analyticsValue,
    )

    data class LinesViewed(val trigger: NavigationTrigger) : SevEvent(
        "Lines Viewed",
        "trigger" to trigger.analyticsValue,
    )

    data class CardsViewed(val trigger: NavigationTrigger) : SevEvent(
        "Cards Viewed",
        "trigger" to trigger.analyticsValue,
    )

    data class CardsHelpViewed(val trigger: NavigationTrigger) : SevEvent(
        "Cards Help Viewed",
        "trigger" to trigger.analyticsValue,
    )

    data class LineStopsViewed(val lineId: LineId, val routeId: RouteId?, val trigger: NavigationTrigger) : SevEvent(
        "Line Stops Viewed",
        "lineId" to lineId,
        "routeId" to routeId,
        "trigger" to trigger.analyticsValue,
    )

    data class StopDetailsViewed(
        val stopId: StopId,
        val source: String,
        val highlightedLineId: LineId?,
        val trigger: NavigationTrigger,
    ) : SevEvent(
        "Stop Details Viewed",
        "stopId" to stopId,
        "source" to source,
        "highlightedLineId" to highlightedLineId,
        "trigger" to trigger.analyticsValue,
    ) {
        companion object {
            const val SOURCE_BACK = "back"
        }
    }

    data class EditFavoritesViewed(val trigger: NavigationTrigger) : SevEvent(
        "Edit Favorites Viewed",
        "trigger" to trigger.analyticsValue,
    )

    data class SearchViewed(val trigger: NavigationTrigger) : SevEvent(
        "Search Viewed",
        "trigger" to trigger.analyticsValue,
    )

    data class SettingsViewed(val trigger: NavigationTrigger) : SevEvent(
        "Settings Viewed",
        "trigger" to trigger.analyticsValue,
    )

    data class StopDetailsClosed(val stopId: StopId, val durationSeconds: Long, val refreshes: Int) : SevEvent(
        "Stop Details Closed",
        "stopId" to stopId,
        "durationSeconds" to durationSeconds,
        "refreshes" to refreshes,
    )
}
