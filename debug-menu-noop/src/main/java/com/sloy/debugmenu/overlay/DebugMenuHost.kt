package com.sloy.debugmenu.overlay

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sloy.debugmenu.base.DebugMenuScope

@Composable
fun DebugMenuHost(
    overlayLogger: OverlayLogger,
    menu: @Composable DebugMenuScope.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    content()
}
