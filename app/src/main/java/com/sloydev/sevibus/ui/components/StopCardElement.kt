package com.sloydev.sevibus.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sloydev.sevibus.domain.model.LineSummary
import com.sloydev.sevibus.domain.model.Stop
import com.sloydev.sevibus.domain.model.description1
import com.sloydev.sevibus.domain.model.description2
import com.sloydev.sevibus.ui.theme.AlexGreyIcons

@Composable
fun StopCardElement(stop: Stop, onStopClick: (Stop) -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = { onStopClick(stop) },
        modifier = modifier
    ) {
        ListItem(
            modifier = Modifier.padding(vertical = 8.dp),
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            headlineContent = {
                Column {
                    Text(stop.description1, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    stop.description2?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = AlexGreyIcons)
                    }
                }
            },
            supportingContent = {
                Text("Parada " + stop.code, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Light)
            },
            trailingContent = { SupportingLines(stop.lines) },
        )
    }
}

@Composable
private fun SupportingLines(lines: List<LineSummary>) {
    Row {
        lines.forEach { line ->
            LineIndicatorSmall(line = line)
            Spacer(Modifier.size(4.dp))
        }
    }
}