package com.sloy.sevibus.feature.foryou.favorites

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/**
 * Screenshot tests for FavoritesWidget.
 * These tests reference preview functions defined in the main source set.
 */
class FavoritesWidgetScreenshotTests {

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
    fun notLoggedPreview() {
        NotLoggedPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun favoritesWidgetLoadingPreview() {
        FavoritesWidgetLoadingPreview()
    }
}
