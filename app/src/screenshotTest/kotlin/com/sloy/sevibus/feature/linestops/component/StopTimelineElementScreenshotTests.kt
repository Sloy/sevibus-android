package com.sloy.sevibus.feature.linestops.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/**
 * Screenshot tests for StopTimelineElement.
 * These tests reference preview functions defined in the main source set.
 */
class StopTimelineElementScreenshotTests {

    @Preview
    @PreviewTest
    @Composable
    fun stopListItemPreview() {
        StopListItemPreview()
    }
}
