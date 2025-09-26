package com.sloy.sevibus.feature.debug

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.sloy.debugmenu.base.DebugMenu
import com.sloy.debugmenu.base.DebugMenuViewModel
import com.sloy.sevibus.feature.debug.auth.AuthDebugModule
import com.sloy.sevibus.feature.debug.location.LocationDebugModule
import com.sloy.sevibus.feature.debug.network.NetworkDebugModule
import com.sloy.sevibus.feature.debug.tracking.TrackingDebugModule
import com.sloy.sevibus.feature.debug.inappreview.InAppReviewDebugModule
import com.sloy.sevibus.ui.preview.ScreenPreview
import com.sloy.sevibus.ui.theme.SevTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun SevDebugMenu() {
    val viewModel = koinViewModel<DebugMenuViewModel>()
    SevDebugMenu({ viewModel })
}

@Composable
private fun SevDebugMenu(viewModelProvider: () -> DebugMenuViewModel) {
    SevTheme {
        DebugMenu(viewModelProvider) {
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
        SevDebugMenu({ error("No viewmodel on preview") })
    }
}
