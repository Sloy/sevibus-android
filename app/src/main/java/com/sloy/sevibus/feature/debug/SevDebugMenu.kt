package com.sloy.sevibus.feature.debug

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.sloy.debugmenu.DebugMenu
import com.sloy.debugmenu.DebugMenuViewModel
import com.sloy.sevibus.feature.debug.location.LocationDebugModule
import com.sloy.sevibus.feature.debug.network.NetworkDebugModule
import com.sloy.sevibus.ui.theme.SevTheme
import kotlinx.serialization.Serializable
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
            NetworkDebugModule()
            LocationDebugModule()
        }
    }
}

@Serializable
data class LocationDebugState(val isFakeLocation: Boolean = true)


@Preview
@Composable
private fun Preview() {
    SevTheme {
        SevDebugMenu({ TODO() })
    }
}
