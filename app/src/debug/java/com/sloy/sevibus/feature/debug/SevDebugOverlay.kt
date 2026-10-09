package com.sloy.sevibus.feature.debug

import androidx.compose.runtime.Composable
import com.sloy.debugmenu.events.overlay.EventsOverlay
import org.koin.compose.koinInject

@Composable
fun SevDebugOverlay() {
    EventsOverlay(eventStore = koinInject(), dataSource = koinInject())
}
