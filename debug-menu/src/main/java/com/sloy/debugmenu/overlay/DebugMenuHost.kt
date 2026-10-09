package com.sloy.debugmenu.overlay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import com.sloy.debugmenu.base.DebugMenuScope
import com.sloy.debugmenu.base.DebugScreenContent

/**
 * Wraps the app [content] with the debug overlays (the [overlay] slot, anchored [overlayBottomPadding] above the safe drawing
 * area's bottom, and the [overlayLogger] items), the floating debug button and the debug [menu] sheet.
 */
@Composable
fun DebugMenuHost(
    overlayLogger: OverlayLogger,
    menu: @Composable DebugMenuScope.() -> Unit,
    modifier: Modifier = Modifier,
    overlayBottomPadding: Dp = 0.dp,
    overlay: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    var isMenuOpen by rememberSaveable { mutableStateOf(false) }
    var screen by remember { mutableStateOf<DebugScreenContent?>(null) }

    Box(modifier.fillMaxSize()) {
        content()
        Box(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(bottom = overlayBottomPadding)
        ) { overlay() }
        OverlayLoggerLayer(overlayLogger)
        FloatingDebugButton(visible = !isMenuOpen && screen == null, onClick = { isMenuOpen = true })
    }

    if (isMenuOpen) {
        DebugMenuSheet(
            onDismiss = { isMenuOpen = false },
            onOpenScreen = { newScreen ->
                isMenuOpen = false
                screen = newScreen
            },
            menu = menu,
        )
    }

    screen?.let { currentScreen ->
        Dialog(
            onDismissRequest = { screen = null },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
        ) {
            Surface(Modifier.fillMaxSize()) {
                SystemBarsMatchSurface()
                currentScreen { screen = null }
            }
        }
    }
}

@Composable
private fun SystemBarsMatchSurface() {
    val view = LocalView.current
    val lightSurface = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    SideEffect {
        val window = (view.parent as? DialogWindowProvider)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = lightSurface
            isAppearanceLightNavigationBars = lightSurface
        }
    }
}
