package com.sloy.sevibus.feature.debug.tracking

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sloy.debugmenu.base.DebugItem
import com.sloy.debugmenu.base.DebugMenu
import com.sloy.debugmenu.base.DebugMenuScope
import com.sloy.debugmenu.base.DebugModule
import com.sloy.sevibus.ui.preview.ScreenPreview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DebugMenuScope.TrackingDebugModule() {
    if (LocalInspectionMode.current) {
        TrackingDebugModule(TrackingDebugModuleState())
        return
    }
    val vm = koinViewModel<TrackingDebugModuleViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()
    TrackingDebugModule(state, onOverlayChanged = vm::onOverlayChanged)
}

@Composable
private fun DebugMenuScope.TrackingDebugModule(
    state: TrackingDebugModuleState,
    onOverlayChanged: (Boolean) -> Unit = {},
) {
    DebugModule("Tracking", Icons.Default.QueryStats, showBadge = state.isOverlayEnabled) {
        Column {
            DebugItem(
                title = "Overlay",
                subtitle = "Show an overlay of tracked events",
                onClick = { onOverlayChanged(!state.isOverlayEnabled) }
            ) {
                Switch(checked = state.isOverlayEnabled, onCheckedChange = { onOverlayChanged(it) })
            }

        }
    }
}

@Preview
@Composable
private fun Preview() {
    ScreenPreview {
        DebugMenu {
            TrackingDebugModule()
        }
    }
}
