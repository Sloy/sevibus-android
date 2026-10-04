package com.sloy.debugmenu.overlay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sloy.debugmenu.base.DebugMenuScope
import com.sloy.debugmenu.base.DebugScreenContent

/**
 * Wraps the app [content] with the debug overlay, the floating debug button and the debug [menu] sheet.
 */
@Composable
fun DebugMenuHost(
    overlayLogger: OverlayLogger,
    menu: @Composable DebugMenuScope.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var isMenuOpen by rememberSaveable { mutableStateOf(false) }
    var screen by remember { mutableStateOf<DebugScreenContent?>(null) }

    Box(modifier.fillMaxSize()) {
        content()
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
                currentScreen { screen = null }
            }
        }
    }
}
