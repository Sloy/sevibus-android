package com.sloy.sevibus.feature.foryou.favorites.edit

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/**
 * Screenshot tests for EditFavoritesScreen.
 * These tests reference preview functions defined in the main source set.
 */
class EditFavoritesScreenScreenshotTests {

    @Preview
    @PreviewTest
    @Composable
    fun preview() {
        Preview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun favoriteItemPreview() {
        FavoriteItemPreview()
    }
}
