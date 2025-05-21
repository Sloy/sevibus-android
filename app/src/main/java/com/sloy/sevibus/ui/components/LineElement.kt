package com.sloy.sevibus.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.sloy.sevibus.domain.model.Line

@Composable
fun LineElement(line: Line, onLineClick: (Line) -> Unit) {
    ListItem(
        modifier = Modifier.clickable { onLineClick(line) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = { Text(line.description) },
        leadingContent = {
            LineIndicator(line)
        },
    )
}
