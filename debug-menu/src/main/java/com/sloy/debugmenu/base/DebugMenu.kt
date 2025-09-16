package com.sloy.debugmenu.base

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Use this composable to display a debug menu UI. Pass your own modules to the [modules] receiver.
 * Each module should internally use the [DebugModule] composable to handle the basic module UI and expandable behaviour.
 *
 * ```
 * DebugMenu {
 *   DebugModule("My Module") {
 *     // Module content
 *   }
 *   DebugModule("Another Module") {
 *     // Module content
 *   }
 * }
 * ```
 */
@Composable
fun DebugMenu(
    viewModelProvider: () -> DebugMenuViewModel = { TODO("Provide an instance of DebugMenuViewModel") },
    modules: @Composable() (DebugMenuScope.() -> Unit)
) {
    val scope = if (LocalInspectionMode.current) {
        PreviewDebugMenuScope
    } else {
        val vm: DebugMenuViewModel = viewModelProvider()
        object : DebugMenuScope {
            override fun onExpandedChanged(module: String, expanded: Boolean) {
                vm.onExpandedChanged(module, expanded)
            }

            override fun isExpanded(module: String): StateFlow<Boolean> {
                return vm.isExpanded(module)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        scope.modules()
    }
}

/**
 * Use this composable to create a custom debug module that will be included in a [DebugMenu].
 * The [title] should be unique, as it's used as key to store the expanded state.
 *
 * Optionally pass `true` to [showBadge] to show a red indicator next to the module icon. This is helpful to quickly see which module is "active" and the user might want to disable it.
 * The definition of what active means is up to each module implementation.
 */
@Composable
fun DebugMenuScope.DebugModule(
    title: String,
    icon: ImageVector,
    showBadge: Boolean = false,
    expandedContent: @Composable () -> Unit
) {
    val roundedCornerShape = MaterialTheme.shapes.small
    Column(
        Modifier
            .clip(shape = roundedCornerShape)
            .background(Color.Transparent, shape = roundedCornerShape)
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = roundedCornerShape
            ),
    ) {
        val isExpanded by isExpanded(title).collectAsStateWithLifecycle()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 72.dp)
                .semantics(mergeDescendants = true) { }
                .clickable { onExpandedChanged(title, !isExpanded) }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 16.dp)
                    .size(48.dp)
            ) {
                BadgedBox(badge = {
                    if (showBadge) {
                        Badge()
                    }
                }) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = "", tint = MaterialTheme.colorScheme.primary)
                    }
                }

            }

            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))

            Icon(
                if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.padding(start = 16.dp)
            )

        }
        if (isExpanded) {
            HorizontalDivider()
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandIn(expandFrom = Alignment.CenterStart),
            exit = shrinkOut(shrinkTowards = Alignment.CenterStart) + fadeOut(),
        ) {
            Column(Modifier.fillMaxWidth()) {
                expandedContent()
            }
        }
    }
}

/**
 * This scope allows the [DebugModue] composable to coordinate the expanded state with the parent [DebugMenu].
 */
interface DebugMenuScope {
    fun onExpandedChanged(module: String, expanded: Boolean)
    fun isExpanded(module: String): StateFlow<Boolean>
}

/**
 * This scope is only intended for Preview mode, so it doesn't require a [DebugMenuViewModel].
 */
object PreviewDebugMenuScope : DebugMenuScope {
    private val expandedState = MutableStateFlow(true)
    override fun onExpandedChanged(module: String, expanded: Boolean) {
        expandedState.value = expanded
    }

    override fun isExpanded(module: String): StateFlow<Boolean> = expandedState
}
