package com.sloy.sevibus.feature.debug.inappreview

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sloy.debugmenu.base.DebugItem
import com.sloy.debugmenu.base.DebugMenu
import com.sloy.debugmenu.base.DebugMenuScope
import com.sloy.debugmenu.base.DebugModule
import com.sloy.sevibus.ui.preview.ScreenPreview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DebugMenuScope.InAppReviewDebugModule() {
    if (LocalInspectionMode.current) {
        InAppReviewDebugModule(InAppReviewDebugModuleState())
        return
    }
    val vm = koinViewModel<InAppReviewDebugModuleViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()
    InAppReviewDebugModule(state, onInAppReviewEnabledChanged = vm::onInAppReviewEnabledChanged)
}

@Composable
private fun DebugMenuScope.InAppReviewDebugModule(
    state: InAppReviewDebugModuleState,
    onInAppReviewEnabledChanged: (Boolean) -> Unit = {},
) {
    DebugModule("In-App Review", Icons.Default.Star, showBadge = !state.isInAppReviewEnabled) {
        Column {
            DebugItem(
                title = "Enable In-App Review",
                subtitle = "When disabled, in-app review prompts will never show",
                onClick = { onInAppReviewEnabledChanged(!state.isInAppReviewEnabled) }
            ) {
                Switch(checked = state.isInAppReviewEnabled, onCheckedChange = { onInAppReviewEnabledChanged(it) })
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    ScreenPreview {
        DebugMenu {
            InAppReviewDebugModule()
        }
    }
}