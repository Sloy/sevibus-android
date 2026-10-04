package com.sloy.debugmenu.base

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp

/**
 * Standard row for debug module content: optional [start] slot, [title] with optional [subtitle], optional [end] slot.
 */
@Composable
fun DebugCell(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    start: (@Composable () -> Unit)? = null,
    end: (@Composable () -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .then(if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick) else Modifier)
            .padding(horizontal = 4.dp, vertical = 12.dp),
    ) {
        if (start != null) {
            Box(Modifier.defaultMinSize(minWidth = 48.dp), contentAlignment = Alignment.CenterStart) { start() }
            Spacer(Modifier.width(16.dp))
        }
        TitleSubtitle(title, subtitle, Modifier.weight(1f))
        if (end != null) {
            Spacer(Modifier.width(16.dp))
            Box(Modifier.defaultMinSize(minWidth = 32.dp), contentAlignment = Alignment.CenterEnd) { end() }
        }
    }
}

/**
 * Title in body medium with an optional secondary subtitle.
 */
@Composable
fun TitleSubtitle(title: String, subtitle: String?, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@PreviewLightDark
@Composable
private fun DebugCellPreview() {
    DebugPreviewTheme {
        Column(Modifier.padding(16.dp)) {
            DebugCell("HTTP overlay", subtitle = "Show requests on the overlay", onClick = {}, end = { Switch(true, onCheckedChange = {}) })
            DebugCell("Auto-reset after 1 failure", onClick = {}, start = { Checkbox(false, onCheckedChange = {}) })
        }
    }
}
