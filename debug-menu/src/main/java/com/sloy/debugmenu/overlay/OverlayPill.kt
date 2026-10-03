package com.sloy.debugmenu.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

internal val OverlayPillBackground = Color(0xFFEEEEEE).copy(alpha = 0.8f)
internal val OverlayPillText = Color(0xFF212121)

@Composable
internal fun OverlayPill(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = modifier
            .padding(vertical = 1.dp, horizontal = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(OverlayPillBackground)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        content = content,
    )
}
