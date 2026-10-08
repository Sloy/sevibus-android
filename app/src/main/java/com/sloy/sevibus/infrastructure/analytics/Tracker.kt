package com.sloy.sevibus.infrastructure.analytics

import com.sloy.sevibus.infrastructure.analytics.events.UserProperty

interface Tracker {
    fun track(event: SevEvent)

    fun setUserProperty(property: UserProperty) {}
}
