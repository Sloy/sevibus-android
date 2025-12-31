package com.sloy.sevibus.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/**
 * Screenshot tests for SurfaceButton.
 * These tests reference preview functions defined in the main source set.
 */
class SurfaceButtonScreenshotTests {

    @Preview
    @PreviewTest
    @Composable
    fun textPreview() {
        TextPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun iconPreview() {
        IconPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun smallIconPreview() {
        SmallIconPreview()
    }
}
