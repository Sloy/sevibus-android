package com.sloy.sevibus.infrastructure.reviews.domain

import com.sloy.sevibus.infrastructure.analytics.SevEvent
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for different happy moment criteria implementations.
 * Each criteria can implement its own logic to determine when it's a good moment
 * to ask the user for a review.
 */
interface HappyMomentCriteria {


    /**
     * Human-readable name for this criteria.
     */
    val name: String

    /**
     * Exposes `true` when this criteria's conditions are met for asking for a review.
     */
    fun observeHappyMoment(): StateFlow<Boolean>

    /**
     * Dispatches an event to this criteria. The criteria can choose to handle
     * or ignore the event based on its implementation.
     */
    fun dispatch(event: SevEvent)
}