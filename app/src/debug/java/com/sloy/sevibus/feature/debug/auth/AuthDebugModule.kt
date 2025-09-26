package com.sloy.sevibus.feature.debug.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sloy.debugmenu.base.DebugItem
import com.sloy.debugmenu.base.DebugMenu
import com.sloy.debugmenu.base.DebugMenuScope
import com.sloy.debugmenu.base.DebugModule
import com.sloy.sevibus.infrastructure.SevLogger
import com.sloy.sevibus.ui.preview.ScreenPreview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DebugMenuScope.AuthDebugModule() {
    if (LocalInspectionMode.current) {
        AuthDebugModule(AuthDebugModuleState())
        return
    }
    val vm = koinViewModel<AuthDebugModuleViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()
    AuthDebugModule(state, onFirebaseLogoutClick = vm::onFirebaseLogoutClick)
}

@Composable
private fun DebugMenuScope.AuthDebugModule(
    state: AuthDebugModuleState,
    onFirebaseLogoutClick: () -> Unit = {},
) {
    DebugModule("Auth", Icons.Outlined.Security) {
        DebugItem(
            title = "Firebase logout",
            subtitle = "Sign out from Firebase Auth (not Google Auth)",
            onClick = {
                SevLogger.logD("AuthDebugModule: DebugItem clicked")
                onFirebaseLogoutClick()
            }
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    ScreenPreview {
        DebugMenu {
            AuthDebugModule()
        }
    }
}
