package com.sloy.sevibus.infrastructure.reviews

import android.app.Activity
import com.google.android.play.core.review.ReviewManager
import com.sloy.sevibus.infrastructure.SevLogger
import kotlinx.coroutines.tasks.await

/**
 * A default implementation of the [InAppReviewManager] interface that uses the Android In-App Reviews API
 * to launch the In-App Review flow.
 *
 * @property reviewManager An instance of [com.google.android.play.core.review.ReviewManager] that will be used to launch the In-App Review flow.
 */
internal class GoogleInAppReviewManager(
    private val reviewManager: ReviewManager
) : InAppReviewManager {

    override suspend fun launchReviewFlow(
        activity: Activity,
    ): Result<Unit> {
        return runCatching {
            val reviewInfo = reviewManager.requestReviewFlow().await()!!
            reviewManager.launchReviewFlow(activity, reviewInfo).await()
            Unit
        }.onFailure {
            SevLogger.logW(it, "Failed requesting InAppReview flow")
        }
    }
}