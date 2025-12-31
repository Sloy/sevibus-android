package com.sloy.sevibus.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/**
 * Screenshot tests for IconPicker.
 * These tests reference preview functions defined in the main source set.
 */
class IconPickerScreenshotTests {

    @Preview
    @PreviewTest
    @Composable
    fun iconPickerPreview() {
        IconPickerPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun iconPickerInContextPreview() {
        IconPickerInContextPreview()
    }
}
