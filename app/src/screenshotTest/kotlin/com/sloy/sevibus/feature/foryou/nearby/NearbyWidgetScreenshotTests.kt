package com.sloy.sevibus.feature.foryou.nearby

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/**
 * Screenshot tests for NearbyWidget.
 * These tests reference preview functions defined in the main source set.
 */
class NearbyWidgetScreenshotTests {

    @Preview
    @PreviewTest
    @Composable
    fun withArrivalsPreview() {
        WithArrivalsPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun emptyPreview() {
        EmptyPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun nearbyWidgetLoadingPreview() {
        NearbyWidgetLoadingPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun noPemissionPreview() {
        NoPemissionPreview()
    }
}
