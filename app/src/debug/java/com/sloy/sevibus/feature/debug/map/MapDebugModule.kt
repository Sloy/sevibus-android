package com.sloy.sevibus.feature.debug.map

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sloy.debugmenu.base.DebugCell
import com.sloy.debugmenu.base.DebugMenu
import com.sloy.debugmenu.base.DebugMenuScope
import com.sloy.debugmenu.base.DebugModule
import com.sloy.sevibus.ui.theme.SevTheme
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@Composable
fun DebugMenuScope.MapDebugModule() {
    if (LocalInspectionMode.current) {
        MapDebugModuleContent(MapDebugModuleState())
        return
    }
    val vm = koinViewModel<MapDebugModuleViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()
    MapDebugModuleContent(
        state,
        MapDebugModuleActions(
            onFakeLocationSelected = vm::onFakeLocationSelected,
            onShowMapStateToggled = vm::onShowMapStateToggled,
            onShowCameraToggled = vm::onShowCameraToggled,
            onShowVisibleAreaToggled = vm::onShowVisibleAreaToggled,
            onShowFitBoundsToggled = vm::onShowFitBoundsToggled,
            onHideStopsToggled = vm::onHideStopsToggled,
            onHideBusesToggled = vm::onHideBusesToggled,
        ),
    )
}

private class MapDebugModuleActions(
    val onFakeLocationSelected: (FakeLocation?) -> Unit = {},
    val onShowMapStateToggled: (Boolean) -> Unit = {},
    val onShowCameraToggled: (Boolean) -> Unit = {},
    val onShowVisibleAreaToggled: (Boolean) -> Unit = {},
    val onShowFitBoundsToggled: (Boolean) -> Unit = {},
    val onHideStopsToggled: (Boolean) -> Unit = {},
    val onHideBusesToggled: (Boolean) -> Unit = {},
)

@Composable
private fun DebugMenuScope.MapDebugModuleContent(
    state: MapDebugModuleState,
    actions: MapDebugModuleActions = MapDebugModuleActions(),
) {
    DebugModule("Map", Icons.Outlined.Map, showBadge = state.hasChanges) {
        FakeLocationDropdown(state.fakeLocation, actions.onFakeLocationSelected)
        SwitchCell("Map state", "Chip with the current map state", state.showMapState, actions.onShowMapStateToggled)
        SwitchCell("Camera", "Chip with the zoom level and the camera target", state.showCamera, actions.onShowCameraToggled)
        SwitchCell(
            "Visible area",
            "Outline the map padding (solid) and the area where lines and stops are fitted (dashed), with their centers",
            state.showVisibleArea,
            actions.onShowVisibleAreaToggled,
        )
        SwitchCell("Fit bounds", "Draw the bounds the camera fits for a line or stop", state.showFitBounds, actions.onShowFitBoundsToggled)
        SwitchCell("Hide stop markers", "Stops outside the selected line or stop, to speed up the map", state.hideStops, actions.onHideStopsToggled)
        SwitchCell("Hide bus markers", "To see the line paths", state.hideBuses, actions.onHideBusesToggled)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FakeLocationDropdown(selected: FakeLocation?, onSelected: (FakeLocation?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
    ) {
        OutlinedTextField(
            value = selected?.label ?: "Off",
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text("Fake location") },
            supportingText = { Text(selected?.coordinates() ?: "Use the real device location") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Off") },
                onClick = {
                    onSelected(null)
                    expanded = false
                },
            )
            FakeLocation.entries.forEach { fakeLocation ->
                DropdownMenuItem(
                    text = { Text(fakeLocation.label) },
                    trailingIcon = { Text(fakeLocation.coordinates(), style = MaterialTheme.typography.bodySmall) },
                    onClick = {
                        onSelected(fakeLocation)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun SwitchCell(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    DebugCell(
        title = title,
        subtitle = subtitle,
        onClick = { onCheckedChange(!checked) },
        end = { Switch(checked = checked, onCheckedChange = onCheckedChange) },
    )
}

private fun FakeLocation.coordinates(): String =
    String.format(Locale.US, "%.4f, %.4f", position.latitude, position.longitude)

@PreviewLightDark
@Composable
private fun MapDebugModulePreview() {
    SevTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            DebugMenu {
                MapDebugModuleContent(MapDebugModuleState(fakeLocation = FakeLocation.Centro, showCamera = true, hideBuses = true))
            }
        }
    }
}
