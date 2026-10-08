package com.sloy.sevibus.infrastructure.analytics.tracker

import com.sloy.sevibus.infrastructure.SevLogger
import com.sloy.sevibus.infrastructure.analytics.SevEvent
import com.sloy.sevibus.infrastructure.analytics.Tracker
import com.sloy.sevibus.infrastructure.analytics.events.UserProperty

class LoggerTracker() : Tracker {

    override fun track(event: SevEvent) {
        SevLogger.logD("Tracked event: ${event.name} ${event.properties.toMap()}")
    }

    override fun setUserProperty(property: UserProperty) {
        SevLogger.logD("Set user property: ${property.name}=${property.value}")
    }

}