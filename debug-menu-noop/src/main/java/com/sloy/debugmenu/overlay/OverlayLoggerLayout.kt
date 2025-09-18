package com.sloy.debugmenu.overlay

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun OverlayLoggerLayout(
    overlayLoggerStateHolder: OverlayLoggerStateHolder,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    content()
}