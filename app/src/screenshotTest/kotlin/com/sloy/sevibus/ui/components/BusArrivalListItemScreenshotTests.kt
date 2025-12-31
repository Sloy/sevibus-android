package com.sloy.sevibus.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewTest

/**
 * Screenshot tests for BusArrivalListItem component.
 * These tests reference preview functions defined in the main source set.
 */
class BusArrivalListItemScreenshotTests {

    @PreviewTest
    @Composable
    fun availablePreview() {
        BusArrivalAvailablePreview()
    }

    @PreviewTest
    @Composable
    fun lastBusPreview() {
        BusArrivalLastBusPreview()
    }

    @PreviewTest
    @Composable
    fun highlightedPreview() {
        BusArrivalHighlightedPreview()
    }

    @PreviewTest
    @Composable
    fun notAvailablePreview() {
        BusArrivalNotAvailablePreview()
    }

    @PreviewTest
    @Composable
    fun loadingWithLinePreview() {
        BusArrivalLoadingWithLinePreview()
    }

    @PreviewTest
    @Composable
    fun loadingPreview() {
        BusArrivalLoadingPreview()
    }
}
