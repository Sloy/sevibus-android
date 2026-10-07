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

val OverlayPillBackground = Color(0xFFEEEEEE).copy(alpha = 0.8f)
val OverlayPillText = Color(0xFF212121)

/**
 * Small translucent chip used by the overlay items, also available to app overlays drawn over its own content.
 */
@Composable
fun OverlayPill(
    modifier: Modifier = Modifier,
    background: Color = OverlayPillBackground,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = modifier
            .padding(vertical = 1.dp, horizontal = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        content = content,
    )
}
