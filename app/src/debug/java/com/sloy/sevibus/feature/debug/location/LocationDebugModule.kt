package com.sloy.sevibus.feature.debug.location

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditLocationAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sloy.debugmenu.base.DebugCell
import com.sloy.debugmenu.base.DebugMenu
import com.sloy.debugmenu.base.DebugMenuScope
import com.sloy.debugmenu.base.DebugModule
import com.sloy.sevibus.ui.theme.SevTheme
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@Composable
fun DebugMenuScope.LocationDebugModule() {
    if (LocalInspectionMode.current) {
        LocationDebugModuleContent(LocationDebugModuleState())
        return
    }
    val vm = koinViewModel<LocationDebugModuleViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()
    LocationDebugModuleContent(state, onFakeLocationSelected = vm::onFakeLocationSelected)
}

@Composable
private fun DebugMenuScope.LocationDebugModuleContent(
    state: LocationDebugModuleState,
    onFakeLocationSelected: (FakeLocation?) -> Unit = {},
) {
    DebugModule("Location", Icons.Outlined.EditLocationAlt, showBadge = state.fakeLocation != null) {
        Column(Modifier.selectableGroup()) {
            LocationOption(
                title = "Off",
                subtitle = "Use the real device location",
                selected = state.fakeLocation == null,
                onClick = { onFakeLocationSelected(null) },
            )
            FakeLocation.entries.forEach { fakeLocation ->
                LocationOption(
                    title = fakeLocation.label,
                    subtitle = String.format(Locale.US, "%.4f, %.4f", fakeLocation.position.latitude, fakeLocation.position.longitude),
                    selected = state.fakeLocation == fakeLocation,
                    onClick = { onFakeLocationSelected(fakeLocation) },
                )
            }
        }
    }
}

@Composable
private fun LocationOption(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    DebugCell(
        title = title,
        subtitle = subtitle,
        onClick = onClick,
        start = { RadioButton(selected = selected, onClick = null) },
    )
}

@PreviewLightDark
@Composable
private fun LocationDebugModulePreview() {
    SevTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            DebugMenu {
                LocationDebugModuleContent(LocationDebugModuleState(fakeLocation = FakeLocation.Centro))
            }
        }
    }
}
