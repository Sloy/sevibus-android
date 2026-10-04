package com.sloy.debugmenu.overlay

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Item displayed in the debug overlay. Putting an item with an existing [id] replaces it.
 * Items with [autoHide] are removed after a short delay.
 */
interface OverlayLoggerItem {
    val id: String
    val autoHide: Boolean

    @Composable
    fun Content(modifier: Modifier)
}
