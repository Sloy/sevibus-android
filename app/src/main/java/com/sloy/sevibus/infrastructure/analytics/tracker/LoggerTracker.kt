package com.sloy.sevibus.infrastructure.analytics.tracker

import com.sloy.sevibus.infrastructure.SevLogger
import com.sloy.sevibus.infrastructure.analytics.SevEvent
import com.sloy.sevibus.infrastructure.analytics.Tracker

class LoggerTracker() : Tracker {

    override fun track(event: SevEvent) {
        SevLogger.logD("Tracked event: ${event.name} ${event.properties.toMap()}")
    }

}