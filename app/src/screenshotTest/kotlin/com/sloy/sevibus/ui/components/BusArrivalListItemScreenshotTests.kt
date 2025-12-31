package com.sloy.sevibus.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/**
 * Screenshot tests for BusArrivalListItem component.
 * These tests reference preview functions defined in the main source set.
 */
class BusArrivalListItemScreenshotTests {

    @Preview
    @PreviewTest
    @Composable
    fun availablePreview() {
        BusArrivalAvailablePreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun lastBusPreview() {
        BusArrivalLastBusPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun highlightedPreview() {
        BusArrivalHighlightedPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun notAvailablePreview() {
        BusArrivalNotAvailablePreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun loadingWithLinePreview() {
        BusArrivalLoadingWithLinePreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun loadingPreview() {
        BusArrivalLoadingPreview()
    }
}
