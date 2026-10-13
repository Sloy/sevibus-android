package com.sloy.debugmenu.events.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun OverlayPreviewBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        Modifier
            .size(390.dp, 844.dp)
            .background(Color(0xFFE9EAEE))
            .padding(bottom = 124.dp),
        content = content,
    )
}
