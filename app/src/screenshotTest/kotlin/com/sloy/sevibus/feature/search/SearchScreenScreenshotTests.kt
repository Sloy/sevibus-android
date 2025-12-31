package com.sloy.sevibus.feature.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/**
 * Screenshot tests for SearchScreen.
 * These tests reference preview functions defined in the main source set.
 */
class SearchScreenScreenshotTests {

    @Preview
    @PreviewTest
    @Composable
    fun previewResults() {
        PreviewResults()
    }

    @Preview
    @PreviewTest
    @Composable
    fun previewEmpty() {
        PreviewEmpty()
    }
}
