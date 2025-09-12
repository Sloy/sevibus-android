package com.sloy.sevibus.infrastructure.analytics.events

import com.sloy.sevibus.infrastructure.analytics.SevEvent

class ForYouScreen() : SevEvent(
    "For You Viewed"
)

class StopDetailsScreen(val stopId: Int) : SevEvent(
    "Stop Details Viewed",
    "stopId" to stopId
)