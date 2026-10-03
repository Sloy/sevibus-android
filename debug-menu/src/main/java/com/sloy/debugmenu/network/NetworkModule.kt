package com.sloy.debugmenu.network

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sloy.debugmenu.base.DebugCell
import com.sloy.debugmenu.base.DebugMenu
import com.sloy.debugmenu.base.DebugMenuScope
import com.sloy.debugmenu.base.DebugModule
import com.sloy.debugmenu.base.DebugPreviewTheme
import com.sloy.debugmenu.base.PillSegmentedControl
import com.sloy.debugmenu.base.TitleSubtitle
import com.sloy.debugmenu.overlay.OverlayLogger

/**
 * Network section: HTTP overlay, forced failures, latency and API host override.
 */
@Composable
fun DebugMenuScope.NetworkModule(
    dataSource: NetworkDebugModuleDataSource,
    overlayLogger: OverlayLogger,
    hostPresets: List<HostPreset>,
) {
    val viewModel = viewModel { NetworkDebugModuleViewModel(dataSource, overlayLogger, hostPresets) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    NetworkModuleContent(
        state = state,
        hostPresets = viewModel.hostPresets,
        onHttpOverlayToggled = viewModel::onHttpOverlayToggled,
        onForceFailureToggled = viewModel::onForceFailureToggled,
        onAutoResetToggled = viewModel::onAutoResetToggled,
        onLatencySelected = viewModel::onLatencySelected,
        onHostSelected = viewModel::onHostSelected,
        onCustomHostApplied = viewModel::onCustomHostApplied,
    )
}

@Composable
private fun DebugMenuScope.NetworkModuleContent(
    state: NetworkDebugModuleState,
    hostPresets: List<HostPreset>,
    onHttpOverlayToggled: (Boolean) -> Unit = {},
    onForceFailureToggled: (Boolean) -> Unit = {},
    onAutoResetToggled: (Boolean) -> Unit = {},
    onLatencySelected: (LatencyPreset) -> Unit = {},
    onHostSelected: (String?) -> Unit = {},
    onCustomHostApplied: (String) -> Boolean = { true },
) {
    DebugModule("Network", Icons.Outlined.Wifi, showBadge = state.isAnyFeatureActive()) {
        DebugCell(
            title = "HTTP overlay",
            subtitle = "Show requests on the overlay",
            onClick = { onHttpOverlayToggled(!state.isHttpOverlayEnabled) },
            end = { Switch(checked = state.isHttpOverlayEnabled, onCheckedChange = onHttpOverlayToggled) },
        )
        DebugCell(
            title = "Force failure",
            subtitle = "Returns 500 for every request",
            onClick = { onForceFailureToggled(!state.isForceFailureEnabled) },
            end = { Switch(checked = state.isForceFailureEnabled, onCheckedChange = onForceFailureToggled) },
        )
        AnimatedVisibility(visible = state.isForceFailureEnabled) {
            DebugCell(
                title = "Auto-reset after 1 failure",
                onClick = { onAutoResetToggled(!state.autoResetForceFailure) },
                start = { Checkbox(checked = state.autoResetForceFailure, onCheckedChange = onAutoResetToggled) },
            )
        }
        Column(Modifier.padding(horizontal = 4.dp, vertical = 12.dp)) {
            TitleSubtitle("Latency", state.latencyPreset.subtitle())
            Spacer(Modifier.height(8.dp))
            PillSegmentedControl(
                options = LatencyPreset.entries.map { it.label },
                selectedIndex = state.latencyPreset.ordinal,
                onSelected = { index -> onLatencySelected(LatencyPreset.entries[index]) },
            )
        }
        HostSelector(state.hostOverride, hostPresets, onHostSelected, onCustomHostApplied)
    }
}

@Composable
private fun HostSelector(
    hostOverride: String?,
    hostPresets: List<HostPreset>,
    onHostSelected: (String?) -> Unit,
    onCustomHostApplied: (String) -> Boolean,
) {
    val options = listOf("Default") + hostPresets.map { it.label } + "Custom"
    val customIndex = options.lastIndex
    val storedIndex = hostSelectionIndex(hostOverride, hostPresets)
    var isCustomSelected by rememberSaveable(storedIndex) { mutableStateOf(storedIndex == customIndex) }
    val selectedIndex = if (isCustomSelected) customIndex else storedIndex

    Column(Modifier.padding(horizontal = 4.dp, vertical = 12.dp)) {
        TitleSubtitle("Host", hostOverride ?: "App default")
        Spacer(Modifier.height(8.dp))
        PillSegmentedControl(
            options = options,
            selectedIndex = selectedIndex,
            onSelected = { index ->
                when (index) {
                    0 -> {
                        isCustomSelected = false
                        onHostSelected(null)
                    }
                    customIndex -> isCustomSelected = true
                    else -> {
                        isCustomSelected = false
                        onHostSelected(hostPresets[index - 1].url)
                    }
                }
            },
        )
        AnimatedVisibility(visible = isCustomSelected) {
            CustomHostField(
                initialValue = if (storedIndex == customIndex) hostOverride.orEmpty() else "",
                onApply = onCustomHostApplied,
            )
        }
    }
}

@Composable
private fun CustomHostField(initialValue: String, onApply: (String) -> Boolean) {
    val context = LocalContext.current
    var value by rememberSaveable(initialValue) { mutableStateOf(initialValue) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(Modifier.padding(top = 8.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                value = it
                error = null
            },
            label = { Text("Custom host") },
            placeholder = { Text("http://192.168.1.10:8080") },
            singleLine = true,
            isError = error != null,
            supportingText = error?.let { message -> @Composable { Text(message) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
            trailingIcon = {
                IconButton(onClick = {
                    scanQrCode(
                        context = context,
                        onResult = {
                            value = it
                            error = null
                        },
                        onError = { error = it },
                    )
                }) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR code")
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
        FilledTonalButton(
            onClick = { if (!onApply(value)) error = "Invalid URL" },
            modifier = Modifier.align(Alignment.End),
        ) {
            Text("Apply")
        }
    }
}

private fun LatencyPreset.subtitle(): String = when (this) {
    LatencyPreset.Off -> "Adds a delay to every response"
    LatencyPreset.Standard3G -> "~562ms delay"
    LatencyPreset.Slow3G -> "~2s delay"
}

@PreviewLightDark
@Composable
private fun NetworkModulePreview() {
    DebugPreviewTheme {
        DebugMenu {
            NetworkModuleContent(
                state = NetworkDebugModuleState(isForceFailureEnabled = true, latencyPreset = LatencyPreset.Standard3G),
                hostPresets = listOf(HostPreset("Prod", "https://prod.example.com"), HostPreset("Dev", "https://dev.example.com")),
            )
        }
    }
}
