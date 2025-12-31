package com.sloy.sevibus.feature.lines

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/**
 * Screenshot tests for LinesScreen.
 * These tests reference preview functions defined in the main source set.
 */
class LinesScreenScreenshotTests {

    @Preview
    @PreviewTest
    @Composable
    fun linesScreenPreview() {
        LinesScreenPreview()
    }
}
