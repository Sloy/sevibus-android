package com.sloydev.sevibus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sloydev.sevibus.domain.model.Line

@Composable
fun LineIndicatorSmall(line: Line, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(Color(line.colorHex))
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            line.label,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.bodySmall
                .copy(fontWeight = FontWeight.Medium)
        )
    }
}