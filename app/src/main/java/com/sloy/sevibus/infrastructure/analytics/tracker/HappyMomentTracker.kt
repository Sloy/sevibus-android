package com.sloy.sevibus.infrastructure.analytics.tracker

import com.sloy.sevibus.infrastructure.analytics.SevEvent
import com.sloy.sevibus.infrastructure.analytics.Tracker
import com.sloy.sevibus.infrastructure.reviews.domain.HappyMomentCriteria

class HappyMomentTracker(private val criterias: List<HappyMomentCriteria>) : Tracker {

    override fun track(event: SevEvent) {
        criterias.forEach {
            it.dispatch(event)
        }
    }

}