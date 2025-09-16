package com.sloy.sevibus.feature.debug.network.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import com.sloy.debugmenu.overlay.OverlayLoggerStateHolder

/**
 * This is the actual overlay layout containing the items that are currently displayed on screen.
 * This composable intentionally ignores any touch input.
 *
 * If [content] is provided, the layout will render the content with the overlay on top of it.
 * It can also be ommited to show the overlay individually, for example, inserted in its own FrameLayout.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun OverlayLoggerLayout(
    overlayLoggerStateHolder: OverlayLoggerStateHolder,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val items by overlayLoggerStateHolder.items.collectAsState()
    val isVisible by overlayLoggerStateHolder.isVisible.collectAsState()
    Box {
        content()
        if (isVisible) {
            LazyColumn(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom,
                userScrollEnabled = false,
                modifier = modifier
                    .fillMaxSize()
                    .safeContentPadding()
                    .pointerInteropFilter {
                        return@pointerInteropFilter false
                    },
            ) {
                items.forEach { item ->
                    item(item.id) {
                        item.Content(Modifier.animateItem())
                    }
                }
            }
        }
    }
}
