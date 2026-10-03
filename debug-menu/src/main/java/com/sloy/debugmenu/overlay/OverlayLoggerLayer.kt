package com.sloy.debugmenu.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun OverlayLoggerLayer(overlayLogger: OverlayLogger, modifier: Modifier = Modifier) {
    val items by overlayLogger.items.collectAsStateWithLifecycle()
    if (items.isEmpty()) return
    LazyColumn(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom,
        userScrollEnabled = false,
        modifier = modifier
            .fillMaxSize()
            .safeContentPadding()
            .clearAndSetSemantics {}
            .pointerInteropFilter { false },
    ) {
        items(items, key = { it.id }) { item ->
            item.Content(Modifier.animateItem())
        }
    }
}
