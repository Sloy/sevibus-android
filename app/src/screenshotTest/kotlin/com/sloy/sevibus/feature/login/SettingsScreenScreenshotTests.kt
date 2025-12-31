package com.sloy.sevibus.feature.login

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/**
 * Screenshot tests for SettingsScreen.
 * These tests reference preview functions defined in the main source set.
 */
class SettingsScreenScreenshotTests {

    @Preview
    @PreviewTest
    @Composable
    fun loggedInPreview() {
        LoggedInPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun loggedOutPreview() {
        LoggedOutPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun loggedOutProgressPreview() {
        LoggedOutProgressPreview()
    }
}
