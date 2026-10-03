package com.sloy.sevibus.feature.debug

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.sloy.debugmenu.base.DebugMenu
import com.sloy.sevibus.feature.debug.auth.AuthDebugModule
import com.sloy.sevibus.feature.debug.inappreview.InAppReviewDebugModule
import com.sloy.sevibus.feature.debug.location.LocationDebugModule
import com.sloy.sevibus.feature.debug.network.NetworkDebugModule
import com.sloy.sevibus.feature.debug.tracking.TrackingDebugModule
import com.sloy.sevibus.ui.preview.ScreenPreview
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
fun SevDebugMenu() {
    SevTheme {
        DebugMenu {
            TrackingDebugModule()
            NetworkDebugModule()
            LocationDebugModule()
            InAppReviewDebugModule()
            AuthDebugModule()
        }
    }
}

@Preview
@Composable
private fun Preview() {
    ScreenPreview {
        SevDebugMenu()
    }
}
