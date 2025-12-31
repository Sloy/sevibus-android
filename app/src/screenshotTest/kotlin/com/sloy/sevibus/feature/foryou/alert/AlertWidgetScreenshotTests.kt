package com.sloy.sevibus.feature.foryou.alert

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/**
 * Screenshot tests for AlertWidget.
 * These tests reference preview functions defined in the main source set.
 */
class AlertWidgetScreenshotTests {

    @Preview
    @PreviewTest
    @Composable
    fun alertWidgetPreview() {
        AlertWidgetPreview()
    }
}
