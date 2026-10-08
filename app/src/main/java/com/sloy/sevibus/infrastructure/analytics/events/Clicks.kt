package com.sloy.sevibus.infrastructure.analytics.events

import com.sloy.sevibus.domain.model.LineId
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.infrastructure.analytics.SevEvent

interface Clicks {

    data object AppUpdateDownloadClicked : SevEvent(
        "App Update Download Clicked"
    )

    data object AppUpdateInstallClicked : SevEvent(
        "App Update Install Clicked"
    )

    data class AddFavoriteClicked(val stopId: Int) : SevEvent(
        "Add Favorite Clicked", "stopId" to stopId
    )

    data class RemoveFavoriteClicked(val stopId: Int) : SevEvent(
        "Remove Favorite Clicked", "stopId" to stopId
    )

    data class CardTopUpClicked(val cardType: String, val balance: Int?) : SevEvent(
        "Card Top Up Clicked",
        "cardType" to cardType,
        "balance" to balance
    )

    data class LocationButtonClicked(val state: String) : SevEvent(
        "Location Button Clicked",
        "state" to state,
    )

    data object AnalyticsDisabledClicked : SevEvent(
        "Analytics Disabled Clicked"
    )

    data class FavoriteStopClicked(val stopId: Int) : SevEvent(
        "Favorite Stop Clicked",
        "stopId" to stopId
    )

    data object EditFavoritesClicked : SevEvent(
        "Edit Favorites Clicked"
    )

    data class EditFavoriteLineClicked(val isSelected: Boolean) : SevEvent(
        "Edit Favorite Line Clicked",
        "isSelected" to isSelected
    )

    data class NearbyStopClicked(val stopId: Int) : SevEvent(
        "Nearby Stop Clicked", "stopId" to stopId
    )

    data object NearbyStopsLocationPermissionClicked : SevEvent(
        "Nearby Stops Location Permission Clicked"
    )

    data class ForYouTabClicked(val tab: String) : SevEvent(
        "For You Tab Clicked", "tab" to tab
    )

    data object CardAlertDismissClicked : SevEvent(
        "Card Alert Dismiss Clicked"
    )

    data object CardAlertViewClicked : SevEvent(
        "Card Alert View Clicked"
    )

    data class SearchResultLineClicked(val lineId: LineId) : SevEvent(
        "Search Result Line Clicked", "lineId" to lineId
    )

    data class SearchResultStopClicked(val stopId: StopId) : SevEvent(
        "Search Result Stop Clicked", "stopId" to stopId
    )

    data class LineListClicked(val lineLabel: String) : SevEvent(
        "Line List Clicked", "lineLabel" to lineLabel
    )

    data object FeedbackClicked : SevEvent(
        "Feedback Clicked"
    )

    data class MapStopClicked(val stopId: StopId) : SevEvent(
        "Map Stop Clicked", "stopId" to stopId
    )

    data class LineRouteStopClicked(val lineId: LineId, val stopId: StopId) : SevEvent(
        "Line Route Stop Clicked",
        "lineId" to lineId,
        "stopId" to stopId,
    )

    data class ArrivalClicked(val lineId: LineId, val stopId: StopId) : SevEvent(
        "Arrival Clicked",
        "lineId" to lineId,
        "stopId" to stopId,
    )

    data object LogoutClicked : SevEvent(
        "Logout Clicked"
    )

}
