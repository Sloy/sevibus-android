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
}
