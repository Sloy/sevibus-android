package com.sloy.sevibus.feature.debug.network

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sloy.sevibus.modules.tracking.NetworkDebugModuleState
import com.sloy.sevibus.modules.tracking.NetworkDebugModuleViewModel
import com.sloy.debugmenu.base.DebugItem
import com.sloy.debugmenu.base.DebugMenu
import com.sloy.debugmenu.base.DebugMenuScope
import com.sloy.debugmenu.base.DebugModule
import com.sloy.sevibus.ui.components.SegmentedControl
import com.sloy.sevibus.ui.theme.SevTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DebugMenuScope.NetworkDebugModule() {
    if (LocalInspectionMode.current) {
        NetworkDebugModule(NetworkDebugModuleState())
        return
    }
    val vm = koinViewModel<NetworkDebugModuleViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()
    NetworkDebugModule(state, onOverlayChanged = vm::onOverlayChanged)
}

@Composable
fun DebugMenuScope.NetworkDebugModule(
    state: NetworkDebugModuleState,
    onOverlayChanged: (Boolean) -> Unit = {},
) {
    DebugModule("Network", Icons.Default.NetworkCheck, showBadge = state.isOverlayEnabled) {
        Column(Modifier.padding(bottom = 16.dp)) {
            DebugItem(
                title = "Overlay",
                subtitle = "Show outgoing request as an overlay",
                onClick = { onOverlayChanged(!state.isOverlayEnabled) }
            ) {
                Switch(checked = state.isOverlayEnabled, onCheckedChange = { onOverlayChanged(it) })
            }

            HorizontalDivider()

            var selectedIndex by remember { mutableIntStateOf(3) }
            Text("Host:", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 16.dp, top = 8.dp))
            SegmentedControl(
                listOf("Prod", "Stage", "Dev", "Custom"),
                selectedIndex = selectedIndex,
                onOptionSelected = { index -> selectedIndex = index },
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
            )
            if (selectedIndex == 3) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = "http://",
                        onValueChange = {},
                        label = { Text("Custom host") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                    )
                    IconButton(onClick = {
                        //TODO launch QR scanner and insert scanned url in value
                    }, modifier = Modifier.offset(x = -4.dp)) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR")
                    }
                }
            }

        }
    }
}


@Preview
@Composable
private fun Preview() {
    SevTheme {
        DebugMenu {
            NetworkDebugModule()
        }
    }
}
