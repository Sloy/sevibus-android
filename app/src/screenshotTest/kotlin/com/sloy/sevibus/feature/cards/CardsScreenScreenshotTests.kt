package com.sloy.sevibus.feature.cards

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/**
 * Screenshot tests for CardsScreen.
 * These tests reference preview functions defined in the main source set.
 */
class CardsScreenScreenshotTests {

    @Preview
    @PreviewTest
    @Composable
    fun loadedWithTransactionsPreview() {
        LoadedWithTransactionsPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun loadedWithTransactionsLoadingPreview() {
        LoadedWithTransactionsLoadingPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun loadedWithTransactionsEmptyPreview() {
        LoadedWithTransactionsEmptyPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun loadedWithTransactionsErrorPreview() {
        LoadedWithTransactionsErrorPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun loadedPreview() {
        LoadedPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun reorderingPreview() {
        ReorderingPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun loadingPreview() {
        LoadingPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun emptyNfcEnabledPreview() {
        EmptyNfcEnabledPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun emptyCheckingCardPreview() {
        EmptyCheckingCardPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun emptyNfcDisabledPreview() {
        EmptyNfcDisabledPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun errorPreview() {
        ErrorPreview()
    }
}
