package com.sloy.sevibus.feature.cards

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/**
 * Screenshot tests for CardTransactionsElement.
 * These tests reference preview functions defined in the main source set.
 */
class CardTransactionsElementScreenshotTests {

    @Preview
    @PreviewTest
    @Composable
    fun cardTransactionsCardPreview() {
        CardTransactionsCardPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun cardTransactionsLoadingPreview() {
        CardTransactionsLoadingPreview()
    }
}
