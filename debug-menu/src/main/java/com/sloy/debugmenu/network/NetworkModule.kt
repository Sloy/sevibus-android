package com.sloy.debugmenu.network

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontFamily
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
import okhttp3.Cache
import java.util.Locale

/**
 * Network section: HTTP overlay, forced failures, latency, API host override and one-shot network tools.
 *
 * The first host preset is the default host, used when no override is stored.
 * [httpCache] enables the "Clear HTTP cache" action, which also drops stored ETags.
 * [healthCheck] enables the "Health check" action; it returns the label/value pairs to display.
 */
@Composable
fun DebugMenuScope.NetworkModule(
    dataSource: NetworkDebugModuleDataSource,
    overlayLogger: OverlayLogger,
    hostPresets: List<HostPreset>,
    httpCache: Cache? = null,
    healthCheck: (suspend () -> Map<String, String>)? = null,
) {
    val viewModel = viewModel { NetworkDebugModuleViewModel(dataSource, overlayLogger, hostPresets, httpCache, healthCheck) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val toolsState by viewModel.toolsState.collectAsStateWithLifecycle()
    NetworkModuleContent(
        state = state,
        toolsState = toolsState,
        isHealthCheckAvailable = viewModel.isHealthCheckAvailable,
        hostPresets = viewModel.hostPresets,
        onHttpOverlayToggled = viewModel::onHttpOverlayToggled,
        onForceFailureToggled = viewModel::onForceFailureToggled,
        onAutoResetToggled = viewModel::onAutoResetToggled,
        onLatencySelected = viewModel::onLatencySelected,
        onHostSelected = viewModel::onHostSelected,
        onCustomHostApplied = viewModel::onCustomHostApplied,
        onClearHttpCacheClicked = viewModel::onClearHttpCacheClicked,
        onHealthCheckClicked = viewModel::onHealthCheckClicked,
    )
}

@Composable
private fun DebugMenuScope.NetworkModuleContent(
    state: NetworkDebugModuleState,
    toolsState: NetworkToolsState,
    isHealthCheckAvailable: Boolean,
    hostPresets: List<HostPreset>,
    onHttpOverlayToggled: (Boolean) -> Unit = {},
    onForceFailureToggled: (Boolean) -> Unit = {},
    onAutoResetToggled: (Boolean) -> Unit = {},
    onLatencySelected: (LatencyPreset) -> Unit = {},
    onHostSelected: (String) -> Unit = {},
    onCustomHostApplied: (String) -> Boolean = { true },
    onClearHttpCacheClicked: () -> Unit = {},
    onHealthCheckClicked: () -> Unit = {},
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
        if (toolsState.httpCache != HttpCacheState.Unavailable) {
            HttpCacheCell(toolsState.httpCache, onClearHttpCacheClicked)
        }
        if (isHealthCheckAvailable) {
            HealthCheckCell(toolsState.healthCheck, onHealthCheckClicked)
        }
    }
}

@Composable
private fun HttpCacheCell(cacheState: HttpCacheState, onClick: () -> Unit) {
    val subtitle = when (cacheState) {
        HttpCacheState.Unavailable, HttpCacheState.Loading -> "Removes cached responses and ETags"
        is HttpCacheState.Ready -> if (cacheState.justCleared) {
            "Cleared · ${formatBytes(cacheState.sizeBytes)} in use"
        } else {
            "Removes cached responses and ETags · ${formatBytes(cacheState.sizeBytes)} in use"
        }
        is HttpCacheState.Error -> cacheState.message
    }
    DebugCell(
        title = "Clear HTTP cache",
        subtitle = subtitle,
        enabled = cacheState != HttpCacheState.Loading,
        onClick = onClick,
        end = { ActionIndicator(isLoading = cacheState == HttpCacheState.Loading) { Icon(Icons.Outlined.DeleteSweep, contentDescription = null) } },
    )
}

@Composable
private fun HealthCheckCell(healthState: HealthCheckState, onClick: () -> Unit) {
    val subtitle = when (healthState) {
        HealthCheckState.Idle, HealthCheckState.Loading -> "Query the server health endpoint"
        is HealthCheckState.Success -> "Tap to refresh"
        is HealthCheckState.Error -> healthState.message
    }
    DebugCell(
        title = "Health check",
        subtitle = subtitle,
        enabled = healthState != HealthCheckState.Loading,
        onClick = onClick,
        end = { ActionIndicator(isLoading = healthState == HealthCheckState.Loading) { Icon(Icons.Outlined.MonitorHeart, contentDescription = null) } },
    )
    AnimatedVisibility(visible = healthState is HealthCheckState.Success) {
        val entries = (healthState as? HealthCheckState.Success)?.entries.orEmpty()
        Column(Modifier.padding(start = 4.dp, end = 4.dp, bottom = 12.dp)) {
            entries.forEach { (label, value) ->
                Row(Modifier.padding(vertical = 2.dp)) {
                    Text(
                        label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(112.dp),
                    )
                    Text(
                        value,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionIndicator(isLoading: Boolean, icon: @Composable () -> Unit) {
    if (isLoading) {
        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
    } else {
        icon()
    }
}

internal fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
    else -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
}

@Composable
private fun HostSelector(
    hostOverride: String?,
    hostPresets: List<HostPreset>,
    onHostSelected: (String) -> Unit,
    onCustomHostApplied: (String) -> Boolean,
) {
    val options = hostPresets.map { it.label } + "Custom"
    val customIndex = hostPresets.size
    val storedIndex = hostSelectionIndex(hostOverride, hostPresets)
    var isCustomSelected by rememberSaveable(storedIndex) { mutableStateOf(storedIndex == customIndex) }
    val selectedIndex = if (isCustomSelected) customIndex else storedIndex

    Column(Modifier.padding(horizontal = 4.dp, vertical = 12.dp)) {
        TitleSubtitle("Host", hostOverride ?: hostPresets.firstOrNull()?.url ?: "App default")
        Spacer(Modifier.height(8.dp))
        PillSegmentedControl(
            options = options,
            selectedIndex = selectedIndex,
            onSelected = { index ->
                if (index == customIndex) {
                    isCustomSelected = true
                } else {
                    isCustomSelected = false
                    onHostSelected(hostPresets[index].url)
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
                toolsState = NetworkToolsState(
                    httpCache = HttpCacheState.Ready(sizeBytes = 2_457_600),
                    healthCheck = HealthCheckState.Success(mapOf("Host" to "prod-1", "Environment" to "production", "Version" to "1.42.0")),
                ),
                isHealthCheckAvailable = true,
                hostPresets = listOf(HostPreset("Prod", "https://prod.example.com"), HostPreset("Dev", "https://dev.example.com")),
            )
        }
    }
}
