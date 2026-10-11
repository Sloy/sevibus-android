package com.sloy.sevibus.feature.cards

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
fun CardPageDots(count: Int, selected: Int?, modifier: Modifier = Modifier) {
    if (count <= 1) return
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(count) { index ->
            val isSelected = index == selected
            val width by animateDpAsState(if (isSelected) 16.dp else 6.dp, label = "dotWidth")
            Box(
                Modifier
                    .size(width = width, height = 6.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) SevTheme.colorScheme.onSurface else SevTheme.colorScheme.outline)
            )
        }
    }
}
