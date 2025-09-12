package com.sloy.sevibus.infrastructure.analytics.events

import com.sloy.sevibus.infrastructure.analytics.SevEvent

class AppUpdateClicked(val stopId: Int) : SevEvent(
    "App Update Clicked"
)

data class AddFavoriteClicked(val stopId: Int) : SevEvent(
    "Add Favorite Clicked", "stopId" to stopId
)