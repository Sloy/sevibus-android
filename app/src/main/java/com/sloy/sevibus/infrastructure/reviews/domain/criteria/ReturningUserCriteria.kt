package com.sloy.sevibus.infrastructure.reviews.domain.criteria

import com.sloy.sevibus.infrastructure.analytics.SevEvent
import com.sloy.sevibus.infrastructure.analytics.events.Events
import com.sloy.sevibus.infrastructure.reviews.domain.AppStartTrackingDataSource
import com.sloy.sevibus.infrastructure.reviews.domain.HappyMomentCriteria
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * "Returning user" happy moment criteria implementation.
 *
 * The conditions are:
 * - The user has opened the app at least 3 times in the past 30 days.
 *
 * Trigger the happy moment when:
 * - App starts (if all conditions are met)
 */
class ReturningUserCriteria(
    private val appStartTracker: AppStartTrackingDataSource
) : HappyMomentCriteria {

    override val name = "Returning user"

    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _happyMoment = MutableStateFlow(false)

    override fun observeHappyMoment(): StateFlow<Boolean> {
        return _happyMoment.asStateFlow()
    }

    override fun dispatch(event: SevEvent) {
        when (event) {
            is Events.AppStarted -> {
                backgroundScope.launch {
                    // Record the app start
                    appStartTracker.recordAppStart()

                    // Check if conditions are met
                    if (appStartTracker.hasEnoughAppStarts()) {
                        // Set happy moment to true and keep it true
                        _happyMoment.value = true
                    }
                }
            }

            else -> { /* Ignore other events */
            }
        }
    }
}