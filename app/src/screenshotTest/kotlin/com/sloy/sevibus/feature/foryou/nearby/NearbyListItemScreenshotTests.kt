package com.sloy.sevibus.feature.foryou.nearby

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/**
 * Screenshot tests for NearbyListItem.
 * These tests reference preview functions defined in the main source set.
 */
class NearbyListItemScreenshotTests {

    @Preview
    @PreviewTest
    @Composable
    fun loadedPreview() {
        LoadedPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun loadingArrivalsPreview() {
        LoadingArrivalsPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun nearbyListItemLoadingPreview() {
        NearbyListItemLoadingPreview()
    }
}
