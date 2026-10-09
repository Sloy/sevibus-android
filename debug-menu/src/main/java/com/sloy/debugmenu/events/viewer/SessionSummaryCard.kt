package com.sloy.debugmenu.events.viewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.sloy.debugmenu.base.DebugPreviewTheme
import com.sloy.debugmenu.base.ScreenshotSuite
import com.sloy.debugmenu.base.ScreenshotTest
import com.sloy.debugmenu.events.CapturedEvent
import com.sloy.debugmenu.events.EventText
import com.sloy.debugmenu.events.EventType
import com.sloy.debugmenu.events.colors
import com.sloy.debugmenu.events.formatSeconds

@Composable
internal fun SessionSummaryCard(event: CapturedEvent, expanded: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val colors = EventType.OTHER.colors()
    val shape = RoundedCornerShape(24.dp)
    val chevron by animateFloatAsState(if (expanded) 180f else 0f, tween(200), label = "sessionChevron")
    Column(
        modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .background(colors.accent.copy(alpha = 0.05f))
    ) {
        Row(Modifier.clickable(onClick = onToggle).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconSpot(Icons.Outlined.DarkMode)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Session Summary", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    event.properties["sessionType"]?.let { type ->
                        Text(
                            type.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.ink,
                            modifier = Modifier.background(colors.tint, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                    event.properties["durationSeconds"]?.toLongOrNull()?.let {
                        Text(formatSeconds(it), style = EventText.Mono12Medium, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Text("· ${event.timestamp}", style = EventText.Mono12, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                modifier = Modifier.size(24.dp).rotate(chevron),
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(tween(200)) + fadeIn(tween(200)),
            exit = shrinkVertically(tween(200)) + fadeOut(tween(200)),
        ) {
            PropertyBox(event.properties, timestampMillis = null, modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp))
        }
    }
}

@Composable
private fun IconSpot(icon: ImageVector) {
    Box(Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
    }
}

private fun sampleSummary(): CapturedEvent = ViewerSampleData.events.first { it.id == ViewerSampleData.sessionSummaryId }

@ScreenshotTest(ScreenshotSuite.Components)
@PreviewLightDark
@Composable
internal fun SessionSummaryCardCollapsedPreview() {
    DebugPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.surface) { SessionSummaryCard(sampleSummary(), expanded = false, onToggle = {}) }
    }
}

@ScreenshotTest(ScreenshotSuite.Components)
@PreviewLightDark
@Composable
internal fun SessionSummaryCardExpandedPreview() {
    DebugPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.surface) { SessionSummaryCard(sampleSummary(), expanded = true, onToggle = {}) }
    }
}

@ScreenshotTest(ScreenshotSuite.Components)
@PreviewLightDark
@Composable
internal fun SessionSummaryCardNoDurationPreview() {
    DebugPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            SessionSummaryCard(
                CapturedEvent("Session Summary", mapOf("sessionType" to "glancer"), timestampMillis = ViewerSampleData.startMillis + 15_000, id = "no-duration"),
                expanded = false,
                onToggle = {},
            )
        }
    }
}
