package com.sloy.sevibus.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/**
 * Screenshot tests for FakeReviewDialog.
 * These tests reference preview functions defined in the main source set.
 */
class FakeReviewDialogScreenshotTests {

    @Preview
    @PreviewTest
    @Composable
    fun fakeReviewDialogContentPreview() {
        FakeReviewDialogContentPreview()
    }
}
