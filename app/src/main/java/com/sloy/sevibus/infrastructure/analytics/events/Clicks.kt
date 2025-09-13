package com.sloy.sevibus.infrastructure.analytics.events

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

    data class CardTopUpClicked(val type: String, val balance: Int?) : SevEvent(
        "Card Top Up Clicked",
        "type" to type,
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

    data class NearbyStopClicked(val stopId: Int) : SevEvent(
        "Nearby Stop Clicked", "stopId" to stopId
    )

    data object NearbyStopsLocationPermissionClicked : SevEvent(
        "Nearby Stops Location Permission Clicked"
    )

    data class ForYouTabClicked(val tab: String) : SevEvent(
        "For You Tab Clicked", "tab" to tab
    )
}
