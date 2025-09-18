package com.sloy.debugmenu.overlay

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Base interface to show items in the overlay view.
 * Implementations should properly implement [equals] to allow proper handling of animations and autoHide.
 *
 * The [id] allows for updating already displayed items (eg. a network request in progress and finished).
 */
interface OverlayItem {
    val id: String
    val autoHide: Boolean

    @Composable
    fun Content(modifier: Modifier)
}
