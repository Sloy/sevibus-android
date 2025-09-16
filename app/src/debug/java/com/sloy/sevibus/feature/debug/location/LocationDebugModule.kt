package com.sloy.sevibus.feature.debug.location

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditLocationAlt
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
import com.sloy.sevibus.ui.theme.SevTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun DebugMenuScope.LocationDebugModule() {
    if (LocalInspectionMode.current) {
        LocationDebugModule(LocationDebugModuleState())
        return
    }
    val vm = koinViewModel<LocationDebugModuleViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()
    LocationDebugModule(state, onFakeLocationChanged = vm::onFakeLocationChanged)
}

@Composable
private fun DebugMenuScope.LocationDebugModule(
    state: LocationDebugModuleState,
    onFakeLocationChanged: (Boolean) -> Unit = {},
) {
    DebugModule("Location", Icons.Outlined.EditLocationAlt, showBadge = state.isFakeLocationEnabled) {

        DebugItem(
            title = "Fake location",
            subtitle = "Use a fake location in Sevilla",
            onClick = { onFakeLocationChanged(!state.isFakeLocationEnabled) }
        ) {
            Switch(checked = state.isFakeLocationEnabled, onCheckedChange = { onFakeLocationChanged(it) })
        }

    }
}


@Preview
@Composable
private fun Preview() {
    SevTheme {
        DebugMenu {
            LocationDebugModule()
        }
    }
}

