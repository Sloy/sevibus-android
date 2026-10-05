package com.sloy.sevibus.feature.debug

import androidx.compose.runtime.Composable
import com.sloy.debugmenu.base.DebugMenuScope
import com.sloy.debugmenu.events.EventsModule
import com.sloy.debugmenu.network.NetworkModule
import com.sloy.sevibus.data.api.AdminApi
import com.sloy.sevibus.feature.debug.admin.AdminDashboardLink
import com.sloy.sevibus.feature.debug.auth.AuthDebugModule
import com.sloy.sevibus.feature.debug.inappreview.InAppReviewDebugModule
import com.sloy.sevibus.feature.debug.location.LocationDebugModule
import com.sloy.sevibus.feature.debug.network.SevHostPresets
import com.sloy.sevibus.feature.debug.network.toDebugEntries
import org.koin.compose.koinInject

@Composable
fun DebugMenuScope.SevDebugMenu() {
    val adminApi: AdminApi = koinInject()
    NetworkModule(
        dataSource = koinInject(),
        overlayLogger = koinInject(),
        hostPresets = SevHostPresets,
        httpCache = koinInject(),
        healthCheck = { adminApi.healthCheck().toDebugEntries() },
    )
    EventsModule(koinInject(), koinInject(), koinInject())
    LocationDebugModule()
    InAppReviewDebugModule()
    AuthDebugModule()
    AdminDashboardLink()
}
