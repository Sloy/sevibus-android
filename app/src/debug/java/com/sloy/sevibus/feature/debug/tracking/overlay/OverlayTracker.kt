package com.sloy.sevibus.feature.debug.tracking.overlay

import com.sloy.debugmenu.overlay.OverlayLoggerStateHolder
import com.sloy.sevibus.feature.debug.tracking.TrackingDebugModuleDataSource
import com.sloy.sevibus.infrastructure.analytics.SevEvent
import com.sloy.sevibus.infrastructure.analytics.Tracker

class OverlayTracker(
    private val trackingModuleDataSource: TrackingDebugModuleDataSource,
    private val overlayLoggerStateHolder: OverlayLoggerStateHolder
) : Tracker {
    override fun track(event: SevEvent) {
        if (trackingModuleDataSource.getCurrentState().isOverlayEnabled) {
            overlayLoggerStateHolder.put(TrackingOverlayItem(event))
        }
    }
}
