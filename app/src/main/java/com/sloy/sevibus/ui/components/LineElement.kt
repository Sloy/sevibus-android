package com.sloy.sevibus.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.Line
import com.sloy.sevibus.domain.model.isCurrentyActive
import com.sloy.sevibus.ui.formatter.TimeFormatter
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
fun LineElement(line: Line, onLineClick: (Line) -> Unit) {
    val inSchedule = line.routes.any { it.schedule.isCurrentyActive() }
    ListItem(
        modifier = Modifier.clickable { onLineClick(line) }.alpha(if(inSchedule)1f else 0.5f),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = { Text(line.description) },
        supportingContent = { Text(TimeFormatter.schedule(line.routes.first().schedule), style = SevTheme.typography.bodyExtraSmall) },
        leadingContent = {
            LineIndicator(line)
        },
    )
}

@Preview
@Composable
fun LineElementPreview() {
    SevTheme {
        val line = Stubs.lines[0]
        Surface {
            LineElement(line = line, onLineClick = {})
        }
    }
}
