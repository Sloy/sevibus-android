package com.sloy.sevibus.feature.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/**
 * Screenshot tests for RoundedSearchBar.
 * These tests reference preview functions defined in the main source set.
 */
class RoundedSearchBarScreenshotTests {

    @Preview
    @PreviewTest
    @Composable
    fun searchPreviewLightDark() {
        SearchPreviewLightDark()
    }

    @Preview
    @PreviewTest
    @Composable
    fun searchEmptyPreviewLightDark() {
        SearchEmptyPreviewLightDark()
    }

    @Preview
    @PreviewTest
    @Composable
    fun searchStopPreviewLightDark() {
        SearchStopPreviewLightDark()
    }

    @Preview
    @PreviewTest
    @Composable
    fun searchLinePreviewLightDark() {
        SearchLinePreviewLightDark()
    }
}
