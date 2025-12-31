package com.sloy.sevibus.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/**
 * Screenshot tests for InfoBannerComponent.
 * These tests reference preview functions defined in the main source set.
 */
class InfoBannerComponentScreenshotTests {

    @Preview
    @PreviewTest
    @Composable
    fun twoLinesPreview() {
        InfoBannerTwoLinesPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun twoLinesActionPreview() {
        InfoBannerTwoLinesActionPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun oneLinePreview() {
        InfoBannerOneLinePreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun oneLineActionPreview() {
        InfoBannerOneLineActionPreview()
    }
}
