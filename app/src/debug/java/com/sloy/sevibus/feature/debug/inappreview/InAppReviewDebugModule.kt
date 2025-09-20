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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
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
                activeCriteria = "Adding favorite",
                availableCriteria = listOf("Adding favorite", "Returning user with favorites", "Returning user", "Always true"),
                debugCriteria = "Always true",
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
        onCriteriaSelected = vm::onCriteriaSelected,
        onRevertToLiveCriteria = vm::onRevertToLiveCriteria
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DebugMenuScope.InAppReviewDebugModule(
    state: InAppReviewDebugModuleState,
    onCriteriaSelected: (String) -> Unit = {},
    onRevertToLiveCriteria: () -> Unit = {},
) {
    DebugModule("In-App Review", Icons.Default.Star, showBadge = state.debugCriteria != null) {
        Column {
            if (state.availableCriteria.isNotEmpty()) {
                var expanded by remember { mutableStateOf(false) }
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            val modeIndicator = if (state.debugCriteria != null) " (debug)" else " (live)"
                            val displayValue = state.activeCriteria ?: "None"

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

                        if (state.debugCriteria != null) {
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

                    DebugItem(title = "Experiment value", subtitle = state.experimentVariant ?: "None") { }
                    DebugItem(title = "Feature flag", subtitle = state.featureFlag?.onOff() ?: "Unknown") { }

                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Current Conditions",
                            style = MaterialTheme.typography.titleSmall,
                        )

                        Text(
                            "Favorites: ${state.favoritesCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Text(
                            "App opens (30d): ${state.appOpensCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Text(
                            "User logged in: ${state.isUserLoggedIn.yesNo()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

private fun Boolean.yesNo(): String = if (this) "Yes" else "No"
private fun Boolean.onOff(): String = if (this) "On" else "Off"

@Preview
@Composable
private fun Preview() {
    ScreenPreview {
        DebugMenu {
            InAppReviewDebugModule()
        }
    }
}