package com.sloy.sevibus.feature.stopdetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/**
 * Screenshot tests for StopDetailScreen.
 * These tests reference preview functions defined in the main source set.
 */
class StopDetailScreenScreenshotTests {

    @Preview
    @PreviewTest
    @Composable
    fun loadedArrivalsPreview() {
        LoadedArrivalsPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun loadingArrivalsPreview() {
        LoadingArrivalsPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun failedArrivalsPreview() {
        FailedArrivalsPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun loadingStopPreview() {
        LoadingStopPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun failedStopPreview() {
        FailedStopPreview()
    }
}
