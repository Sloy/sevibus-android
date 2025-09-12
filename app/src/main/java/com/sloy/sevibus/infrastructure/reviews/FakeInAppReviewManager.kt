package com.sloy.sevibus.infrastructure.reviews

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import com.sloy.sevibus.infrastructure.SevLogger
import com.sloy.sevibus.ui.components.FakeReviewDialogFragment
import kotlin.time.Duration
import kotlin.time.measureTime

/**
 * A fake implementation of the [InAppReviewManager] interface that can be used in debug builds.
 * Displays a dialog similar to the Google Play version, intended for manually testing reviews triggers and flow.
 */
internal class FakeInAppReviewManager : InAppReviewManager {

    override suspend fun launchReviewFlow(
        activity: Activity,
    ): Result<Duration> {
        return runCatching {
            measureTime {
                when (activity) {
                    is AppCompatActivity -> {
                        val dialog = FakeReviewDialogFragment()
                        dialog.show(activity.supportFragmentManager, "FakeReviewDialog")
                        dialog.awaitDismissal()
                    }

                    else -> {
                        throw IllegalArgumentException("Activity must be AppCompatActivity to show dialog fragment")
                    }
                }
            }
        }.onFailure {
            SevLogger.logW(it, "Failed to launch fake review dialog")
        }
    }
}