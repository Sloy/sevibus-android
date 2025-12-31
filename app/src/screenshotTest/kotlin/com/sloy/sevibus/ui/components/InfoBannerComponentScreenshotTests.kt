package com.sloy.sevibus.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewTest

/**
 * Screenshot tests for InfoBannerComponent.
 * These tests reference preview functions defined in the main source set.
 */
class InfoBannerComponentScreenshotTests {

    @PreviewTest
    @Composable
    fun twoLinesPreview() {
        InfoBannerTwoLinesPreview()
    }

    @PreviewTest
    @Composable
    fun twoLinesActionPreview() {
        InfoBannerTwoLinesActionPreview()
    }

    @PreviewTest
    @Composable
    fun oneLinePreview() {
        InfoBannerOneLinePreview()
    }

    @PreviewTest
    @Composable
    fun oneLineActionPreview() {
        InfoBannerOneLineActionPreview()
    }
}
