package com.sloy.sevibus.feature.debug.http

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import com.sloy.sevibus.infrastructure.BuildVariant
import org.koin.compose.koinInject

@Composable
fun HttpOverlayLayout(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    if (BuildVariant.isRelease()) {
        content()
        return
    }
    val httpOverlayState = koinInject<HttpOverlayState>()
    val httpItems by httpOverlayState.items.collectAsState()
    Box {
        content()
        if (httpOverlayState.isVisible) {
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
                httpItems.forEach { httpItem ->
                    item(httpItem.id) {
                        HttpOverlayItemComponent(httpItem, Modifier.animateItem())
                    }
                }
            }
        }
    }
}
