package com.sloy.sevibus.feature.map.icons

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/**
 * Screenshot tests for OutlinedStopIcon.
 * These tests reference preview functions defined in the main source set.
 */
class OutlinedStopIconScreenshotTests {

    @Preview
    @PreviewTest
    @Composable
    fun outlinedStopIconPreview() {
        OutlinedStopIconPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun outlinedStopIconShadowPreview() {
        OutlinedStopIconShadowPreview()
    }
}
