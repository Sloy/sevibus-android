package com.sloy.sevibus.infrastructure.reviews

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.play.core.review.ReviewManagerFactory
import com.sloy.sevibus.infrastructure.BuildVariant

/**
 * Returns an instance of [InAppReviewManager] that can be used to launch the In-App Review flow.
 * In release builds it will return a [GoogleInAppReviewManager] instance,
 * while in debug builds it will return a [FakeInAppReviewManager] instance.
 *
 * @return An instance of [InAppReviewManager].
 */
@Composable
fun rememberInAppReviewManager(): InAppReviewManager {
    if (BuildVariant.isRelease()) {
        val applicationContext = LocalContext.current.applicationContext
        return remember {
            GoogleInAppReviewManager(
                reviewManager = ReviewManagerFactory.create(applicationContext)
            )
        }
    } else {
        return remember {
            FakeInAppReviewManager()
        }
    }
}

@Stable
interface InAppReviewManager {
    /**
     * Launches the In-App Review flow for the given [activity].
     *
     * @param activity The activity from which the In-App Review flow is launched.
     * @param onReviewRequestSuccess A function that will be called if the request to launch the In-App
     * Review flow is successful. If no implementation is provided for this parameter, no action is taken.
     * @param onReviewRequestFail A function that will be called if the request to launch the In-App
     * Review flow fails. If no implementation is provided for this parameter, the exception will be logged
     * and a toast message will be shown to the user.
     */
    suspend fun launchReviewFlow(
        activity: Activity,
    ): Result<Unit>
}


