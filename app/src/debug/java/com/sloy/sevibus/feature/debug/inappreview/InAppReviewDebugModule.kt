package com.sloy.sevibus.feature.debug.inappreview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sloy.debugmenu.base.DebugItem
import com.sloy.debugmenu.base.DebugMenu
import com.sloy.debugmenu.base.DebugMenuScope
import com.sloy.debugmenu.base.DebugModule
import com.sloy.sevibus.ui.preview.ScreenPreview
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugMenuScope.InAppReviewDebugModule() {
    if (LocalInspectionMode.current) {
        InAppReviewDebugModule(
            InAppReviewDebugModuleState(
                activeCriteriaName = "Adding favorite",
                availableCriteria = listOf("Adding favorite", "Returning user with favorites", "Returning user", "Always true"),
                selectedDebugCriteriaName = "Always true",
                favoritesCount = 3,
                appOpensCount = 7,
                isUserLoggedIn = true
            )
        )
        return
    }
    val vm = koinViewModel<InAppReviewDebugModuleViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()
    InAppReviewDebugModule(
        state,
        onInAppReviewEnabledChanged = vm::onInAppReviewEnabledChanged,
        onCriteriaSelected = vm::onCriteriaSelected,
        onRevertToLiveCriteria = vm::onRevertToLiveCriteria
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DebugMenuScope.InAppReviewDebugModule(
    state: InAppReviewDebugModuleState,
    onInAppReviewEnabledChanged: (Boolean) -> Unit = {},
    onCriteriaSelected: (String) -> Unit = {},
    onRevertToLiveCriteria: () -> Unit = {},
) {
    DebugModule("In-App Review", Icons.Default.Star, showBadge = !state.isInAppReviewEnabled) {
        Column {
            DebugItem(
                title = "Enable In-App Review",
                subtitle = "When disabled, in-app review prompts will never show",
                onClick = { onInAppReviewEnabledChanged(!state.isInAppReviewEnabled) }
            ) {
                Switch(checked = state.isInAppReviewEnabled, onCheckedChange = { onInAppReviewEnabledChanged(it) })
            }

            if (state.availableCriteria.isNotEmpty()) {
                var expanded by remember { mutableStateOf(false) }

                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            val modeIndicator = if (state.selectedDebugCriteriaName != null) " (debug)" else " (live)"
                            val displayValue = state.activeCriteriaName ?: "None"

                            OutlinedTextField(
                                value = displayValue,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                label = {
                                    Text("Active criteria$modeIndicator", style = MaterialTheme.typography.titleSmall)
                                },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                            )
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                state.availableCriteria.forEach { criteriaName ->
                                    DropdownMenuItem(
                                        text = { Text(criteriaName) },
                                        onClick = {
                                            onCriteriaSelected(criteriaName)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        if (state.selectedDebugCriteriaName != null) {
                            IconButton(
                                onClick = onRevertToLiveCriteria
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Revert to live criteria"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Current Conditions",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Favorites:", style = MaterialTheme.typography.bodySmall)
                            Text("${state.favoritesCount}", style = MaterialTheme.typography.bodySmall)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("App opens (30d):", style = MaterialTheme.typography.bodySmall)
                            Text("${state.appOpensCount}", style = MaterialTheme.typography.bodySmall)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("User logged in:", style = MaterialTheme.typography.bodySmall)
                            Text(if (state.isUserLoggedIn) "Yes" else "No", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    ScreenPreview {
        DebugMenu {
            InAppReviewDebugModule()
        }
    }
}