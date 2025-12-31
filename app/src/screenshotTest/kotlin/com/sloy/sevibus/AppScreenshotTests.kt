package com.sloy.sevibus

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/**
 * Screenshot tests for App.
 * These tests reference preview functions defined in the main source set.
 */
class AppScreenshotTests {

    @Preview
    @PreviewTest
    @Composable
    fun appPreviewBottomSheet() {
        AppPreviewBottomSheet()
    }

    @Preview
    @PreviewTest
    @Composable
    fun appPreviewFullScreen() {
        AppPreviewFullScreen()
    }
}
