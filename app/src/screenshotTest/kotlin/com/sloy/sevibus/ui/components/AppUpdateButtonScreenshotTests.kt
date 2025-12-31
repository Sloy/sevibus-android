package com.sloy.sevibus.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/**
 * Screenshot tests for AppUpdateButton.
 * These tests reference preview functions defined in the main source set.
 */
class AppUpdateButtonScreenshotTests {

    @Preview
    @PreviewTest
    @Composable
    fun availablePreview() {
        AvailablePreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun downloadingZeroPreview() {
        DownloadingZeroPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun downloadingPreview() {
        DownloadingPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun readyPreview() {
        ReadyPreview()
    }
}
