package com.sloy.sevibus.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/**
 * Screenshot tests for StopCardElement.
 * These tests reference preview functions defined in the main source set.
 */
class StopCardElementScreenshotTests {

    @Preview
    @PreviewTest
    @Composable
    fun singleLinePreview() {
        SingleLinePreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun doubleLinePreview() {
        DoubleLinePreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun highlightedPreview() {
        HighlightedPreview()
    }
}
