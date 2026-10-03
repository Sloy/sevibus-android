package com.sloy.debugmenu.base

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Full-screen debug content. Receives a callback to close itself.
 */
typealias DebugScreenContent = @Composable (onClose: () -> Unit) -> Unit

/**
 * List of debug modules. Each module should use [DebugModule] for the accordion look and behaviour.
 */
@Composable
fun DebugMenu(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    onOpenScreen: (DebugScreenContent) -> Unit = {},
    modules: @Composable DebugMenuScope.() -> Unit,
) {
    val scope: DebugMenuScope = if (LocalInspectionMode.current) {
        PreviewDebugMenuScope
    } else {
        val viewModel: DebugMenuViewModel = viewModel { DebugMenuViewModel() }
        val currentOnDismiss by rememberUpdatedState(onDismiss)
        val currentOnOpenScreen by rememberUpdatedState(onOpenScreen)
        remember(viewModel) {
            object : DebugMenuScope {
                override fun onExpandedChanged(module: String, expanded: Boolean) = viewModel.onExpandedChanged(module, expanded)
                override fun isExpanded(module: String): StateFlow<Boolean> = viewModel.isExpanded(module)
                override fun dismiss() = currentOnDismiss()
                override fun openScreen(content: DebugScreenContent) = currentOnOpenScreen(content)
            }
        }
    }
    Column(modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        scope.modules()
    }
}

/**
 * Accordion section of a [DebugMenu]. The [title] must be unique because it keys the expanded state.
 * [showBadge] marks the section as having something active or overridden.
 */
@Composable
fun DebugMenuScope.DebugModule(
    title: String,
    icon: ImageVector,
    showBadge: Boolean = false,
    expandedContent: @Composable ColumnScope.() -> Unit,
) {
    val expandedFlow = remember(title) { isExpanded(title) }
    val isExpanded by expandedFlow.collectAsStateWithLifecycle()
    val shape = RoundedCornerShape(24.dp)
    val outlineColor by animateColorAsState(
        targetValue = if (isExpanded) MaterialTheme.colorScheme.outlineVariant else Color.Transparent,
        animationSpec = tween(ANIMATION_MILLIS),
        label = "moduleOutline",
    )
    val verticalMargin by animateDpAsState(if (isExpanded) 8.dp else 0.dp, tween(ANIMATION_MILLIS), label = "moduleMargin")
    val chevronRotation by animateFloatAsState(if (isExpanded) 180f else 0f, tween(ANIMATION_MILLIS), label = "moduleChevron")

    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = verticalMargin)
            .border(1.dp, outlineColor, shape)
            .clip(shape)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChanged(title, !isExpanded) }
                .padding(12.dp),
        ) {
            IconSpot(icon, showBadge = showBadge)
            Spacer(Modifier.width(16.dp))
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse $title" else "Expand $title",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.rotate(chevronRotation),
            )
        }
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(tween(ANIMATION_MILLIS)) + fadeIn(tween(ANIMATION_MILLIS)),
            exit = shrinkVertically(tween(ANIMATION_MILLIS)) + fadeOut(tween(ANIMATION_MILLIS)),
        ) {
            Column(Modifier.fillMaxWidth()) {
                HorizontalDivider(Modifier.padding(horizontal = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 16.dp),
                    content = expandedContent,
                )
            }
        }
    }
    if (!isExpanded) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/**
 * Lets modules coordinate with the menu that hosts them.
 */
interface DebugMenuScope {
    fun onExpandedChanged(module: String, expanded: Boolean)
    fun isExpanded(module: String): StateFlow<Boolean>
    fun dismiss()
    fun openScreen(content: DebugScreenContent)
}

/**
 * Scope used in previews. Every module is shown expanded.
 */
object PreviewDebugMenuScope : DebugMenuScope {
    private val expandedState = MutableStateFlow(true)
    override fun onExpandedChanged(module: String, expanded: Boolean) {
        expandedState.value = expanded
    }

    override fun isExpanded(module: String): StateFlow<Boolean> = expandedState
    override fun dismiss() = Unit
    override fun openScreen(content: DebugScreenContent) = Unit
}

private const val ANIMATION_MILLIS = 200

@PreviewLightDark
@Composable
private fun DebugModulePreview() {
    DebugPreviewTheme {
        DebugMenu {
            DebugModule("Network", Icons.Outlined.Wifi, showBadge = true) {
                DebugCell("HTTP overlay", subtitle = "Show requests on the overlay", end = { Switch(true, onCheckedChange = {}) })
            }
        }
    }
}
