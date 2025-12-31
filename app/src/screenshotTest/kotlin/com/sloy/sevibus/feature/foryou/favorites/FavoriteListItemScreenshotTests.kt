package com.sloy.sevibus.feature.foryou.favorites

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/**
 * Screenshot tests for FavoriteListItem.
 * These tests reference preview functions defined in the main source set.
 */
class FavoriteListItemScreenshotTests {

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
    fun favoriteListItemLoadingPreview() {
        FavoriteListItemLoadingPreview()
    }
}
