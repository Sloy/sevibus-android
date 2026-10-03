package com.sloy.sevibus.feature.debug

import androidx.compose.runtime.Composable
import com.sloy.debugmenu.base.DebugMenuScope
import com.sloy.debugmenu.events.EventsModule
import com.sloy.sevibus.feature.debug.auth.AuthDebugModule
import com.sloy.sevibus.feature.debug.inappreview.InAppReviewDebugModule
import com.sloy.sevibus.feature.debug.location.LocationDebugModule
import com.sloy.sevibus.feature.debug.network.NetworkDebugModule
import org.koin.compose.koinInject

@Composable
fun DebugMenuScope.SevDebugMenu() {
    NetworkDebugModule()
    EventsModule(koinInject(), koinInject(), koinInject())
    LocationDebugModule()
    InAppReviewDebugModule()
    AuthDebugModule()
}
