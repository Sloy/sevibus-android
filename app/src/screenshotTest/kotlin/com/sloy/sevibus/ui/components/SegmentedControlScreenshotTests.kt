package com.sloy.sevibus.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/**
 * Screenshot tests for SegmentedControl.
 * These tests reference preview functions defined in the main source set.
 */
class SegmentedControlScreenshotTests {

    @Preview
    @PreviewTest
    @Composable
    fun segmentedControlPreview() {
        SegmentedControlPreview()
    }
}
