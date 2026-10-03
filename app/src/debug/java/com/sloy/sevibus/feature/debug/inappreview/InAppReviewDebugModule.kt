package com.sloy.sevibus.feature.debug.inappreview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DebugMenuScope.InAppReviewDebugModule() {
    if (LocalInspectionMode.current) {
        InAppReviewDebugModuleContent(PreviewState)
        return
    }
    val vm = koinViewModel<InAppReviewDebugModuleViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()
    InAppReviewDebugModuleContent(
        state,
        onCriteriaSelected = vm::onCriteriaSelected,
        onRevertToLiveCriteria = vm::onRevertToLiveCriteria,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DebugMenuScope.InAppReviewDebugModuleContent(
    state: InAppReviewDebugModuleState,
    onCriteriaSelected: (String) -> Unit = {},
    onRevertToLiveCriteria: () -> Unit = {},
) {
    DebugModule("In-App Review", Icons.Outlined.StarOutline, showBadge = state.debugCriteria != null) {
        if (state.availableCriteria.isNotEmpty()) {
            var expanded by remember { mutableStateOf(false) }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.weight(1f),
                ) {
                    val modeIndicator = if (state.debugCriteria != null) " (debug)" else " (live)"
                    OutlinedTextField(
                        value = state.activeCriteria ?: "None",
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        label = { Text("Active criteria$modeIndicator") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(type = MenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        state.availableCriteria.forEach { criteriaName ->
                            DropdownMenuItem(
                                text = { Text(criteriaName) },
                                onClick = {
                                    onCriteriaSelected(criteriaName)
                                    expanded = false
                                },
                            )
                        }
                    }
                }
                if (state.debugCriteria != null) {
                    IconButton(onClick = onRevertToLiveCriteria) {
                        Icon(Icons.Default.Refresh, contentDescription = "Revert to live criteria")
                    }
                }
            }
        }
        DebugCell(title = "Experiment value", subtitle = state.experimentVariant ?: "None")
        DebugCell(title = "Feature flag", subtitle = state.featureFlag?.onOff() ?: "Unknown")
        DebugCell(
            title = "Current conditions",
            subtitle = listOf(
                "Favorites: ${state.favoritesCount}",
                "App opens (30d): ${state.appOpensCount}",
                "User logged in: ${state.isUserLoggedIn.yesNo()}",
            ).joinToString("\n"),
        )
    }
}

private fun Boolean.yesNo(): String = if (this) "Yes" else "No"
private fun Boolean.onOff(): String = if (this) "On" else "Off"

private val PreviewState = InAppReviewDebugModuleState(
    experimentVariant = "Adding favorite",
    featureFlag = true,
    activeCriteria = "Always true",
    debugCriteria = "Always true",
    availableCriteria = listOf("Adding favorite", "Returning user with favorites", "Returning user", "Always true"),
    favoritesCount = 3,
    appOpensCount = 7,
    isUserLoggedIn = true,
)

@PreviewLightDark
@Composable
private fun InAppReviewDebugModulePreview() {
    SevTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            DebugMenu {
                InAppReviewDebugModuleContent(PreviewState)
            }
        }
    }
}
