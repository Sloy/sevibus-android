package com.sloy.debugmenu.overlay

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sloy.debugmenu.base.DebugMenuScope

@Composable
fun DebugMenuHost(
    overlayLogger: OverlayLogger,
    menu: @Composable DebugMenuScope.() -> Unit,
    modifier: Modifier = Modifier,
    overlayBottomPadding: Dp = 0.dp,
    overlay: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    content()
}
