package com.sloy.sevibus.feature.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/**
 * Screenshot tests for MapScreen.
 * These tests reference preview functions defined in the main source set.
 */
class MapScreenScreenshotTests {

    @Preview
    @PreviewTest
    @Composable
    fun mapScreenPreview() {
        MapScreenPreview()
    }
}
