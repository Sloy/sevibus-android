package com.sloy.debugmenu.base

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow

typealias DebugScreenContent = @Composable (onClose: () -> Unit) -> Unit

interface DebugMenuScope {
    fun onExpandedChanged(module: String, expanded: Boolean)
    fun isExpanded(module: String): StateFlow<Boolean>
    fun dismiss()
    fun openScreen(content: DebugScreenContent)
}
