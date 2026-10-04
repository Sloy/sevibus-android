package com.sloy.sevibus.feature.debug.auth

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.sloy.debugmenu.base.DebugCell
import com.sloy.debugmenu.base.DebugMenu
import com.sloy.debugmenu.base.DebugMenuScope
import com.sloy.debugmenu.base.DebugModule
import com.sloy.sevibus.ui.theme.SevTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DebugMenuScope.AuthDebugModule() {
    if (LocalInspectionMode.current) {
        AuthDebugModuleContent()
        return
    }
    val vm = koinViewModel<AuthDebugModuleViewModel>()
    AuthDebugModuleContent(onFirebaseLogoutClick = vm::onFirebaseLogoutClick)
}

@Composable
private fun DebugMenuScope.AuthDebugModuleContent(onFirebaseLogoutClick: () -> Unit = {}) {
    DebugModule("Auth", Icons.Outlined.Security) {
        DebugCell(
            title = "Firebase logout",
            subtitle = "Sign out from Firebase Auth (not Google Auth)",
            onClick = onFirebaseLogoutClick,
            end = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
        )
    }
}

@PreviewLightDark
@Composable
private fun AuthDebugModulePreview() {
    SevTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            DebugMenu {
                AuthDebugModuleContent()
            }
        }
    }
}
