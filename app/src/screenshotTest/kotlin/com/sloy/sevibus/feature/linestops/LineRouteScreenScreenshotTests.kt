package com.sloy.sevibus.feature.linestops

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/**
 * Screenshot tests for LineRouteScreen.
 * These tests reference preview functions defined in the main source set.
 */
class LineRouteScreenScreenshotTests {

    @Preview
    @PreviewTest
    @Composable
    fun previewWithRoutes() {
        PreviewWithRoutes()
    }

    @Preview
    @PreviewTest
    @Composable
    fun previewWithoutRoutes() {
        PreviewWithoutRoutes()
    }
}
