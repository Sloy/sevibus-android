package com.sloy.sevibus.infrastructure.analytics.tracker

import com.sloy.sevibus.infrastructure.analytics.SevEvent
import com.sloy.sevibus.infrastructure.analytics.Tracker
import com.sloy.sevibus.infrastructure.reviews.domain.InAppReviewHappyMomentService

class HappyMomentTracker(private val happyMomentService: InAppReviewHappyMomentService) : Tracker {

    override fun track(event: SevEvent) {
        happyMomentService.dispatch(event)
    }

}