package com.sloy.sevibus.infrastructure.reviews.presentation

import android.app.Activity
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloy.sevibus.infrastructure.SevLogger
import com.sloy.sevibus.infrastructure.analytics.Analytics
import com.sloy.sevibus.infrastructure.analytics.events.Events
import com.sloy.sevibus.infrastructure.reviews.domain.InAppReviewService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class InAppReviewViewModel(
    private val happyMomentService: InAppReviewService,
    private val inAppReviewManager: InAppReviewManager,
    private val analytics: Analytics,
) : ViewModel() {

    val reviewTrigger = happyMomentService.observeHappyMoment()
        .filter { it }
        .map { Unit }
        .shareIn(viewModelScope, SharingStarted.Lazily, 1)

    fun launch(activity: Activity) {
        viewModelScope.launch {
            val result = inAppReviewManager.launchReviewFlow(activity)
            result.onSuccess { duration ->
                SevLogger.logD("Review dialog was displayed for: $duration")
                Toast.makeText(activity, "Displayed for: ${duration.inWholeSeconds} seconds", Toast.LENGTH_LONG).show()
                analytics.track(Events.ReviewDialogDismissed(duration.inWholeSeconds.toInt()))
            }.onFailure { error ->
                SevLogger.logW(error, "Review flow failed: ${error.message}")
                analytics.track(Events.ReviewDialogFailed(error.message ?: "Unknown"))
            }
        }
    }

}