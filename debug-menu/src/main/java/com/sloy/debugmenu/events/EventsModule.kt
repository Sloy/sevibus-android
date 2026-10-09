package com.sloy.debugmenu.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sloy.debugmenu.base.DebugCell
import com.sloy.debugmenu.base.DebugMenu
import com.sloy.debugmenu.base.DebugMenuScope
import com.sloy.debugmenu.base.DebugModule
import com.sloy.debugmenu.base.DebugPreviewTheme
import com.sloy.debugmenu.base.ScreenshotSuite
import com.sloy.debugmenu.base.ScreenshotTest

/**
 * Events section: overlay toggle, timeline rail choice and access to the full-screen event log.
 */
@Composable
fun DebugMenuScope.EventsModule(
    dataSource: EventsDebugModuleDataSource,
    eventStore: EventStore,
) {
    val viewModel = viewModel { EventsDebugModuleViewModel(dataSource, eventStore) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    EventsModuleContent(
        state = state,
        eventCount = events.size,
        onOverlayToggled = viewModel::onOverlayToggled,
        onTimelineRailToggled = viewModel::onTimelineRailToggled,
        onViewAll = { openScreen { onClose -> EventLogScreen(eventStore, onClose) } },
        onClear = viewModel::onClearEvents,
    )
}

@Composable
private fun DebugMenuScope.EventsModuleContent(
    state: EventsDebugModuleState,
    eventCount: Int,
    onOverlayToggled: (Boolean) -> Unit = {},
    onTimelineRailToggled: (Boolean) -> Unit = {},
    onViewAll: () -> Unit = {},
    onClear: () -> Unit = {},
) {
    DebugModule("Events", Icons.Outlined.Insights, showBadge = state.isOverlayEnabled) {
        DebugCell(
            title = "Show events on overlay",
            onClick = { onOverlayToggled(!state.isOverlayEnabled) },
            end = { Switch(checked = state.isOverlayEnabled, onCheckedChange = onOverlayToggled) },
        )
        if (state.isOverlayEnabled) {
            DebugCell(
                title = "Use timeline rail",
                subtitle = "Show events on a vertical timeline instead of a stack",
                onClick = { onTimelineRailToggled(!state.useTimelineRail) },
                start = { Checkbox(checked = state.useTimelineRail, onCheckedChange = onTimelineRailToggled) },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            FilledTonalButton(onClick = onViewAll) {
                Text("View all ($eventCount)")
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
            TextButton(onClick = onClear) {
                Text("Clear")
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@ScreenshotTest(ScreenshotSuite.Components)
@PreviewLightDark
@Composable
internal fun EventsModuleOverlayEnabledPreview() {
    DebugPreviewTheme {
        DebugMenu {
            EventsModuleContent(EventsDebugModuleState(isOverlayEnabled = true), eventCount = 12)
        }
    }
}
