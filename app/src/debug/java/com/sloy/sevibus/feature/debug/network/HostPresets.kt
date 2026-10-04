package com.sloy.sevibus.feature.debug.network

import com.sloy.debugmenu.network.HostPreset
import com.sloy.sevibus.infrastructure.config.ApiConfigurationManager.Companion.DEFAULT_DEBUG_URL
import com.sloy.sevibus.infrastructure.config.ApiConfigurationManager.Companion.DEFAULT_RELEASE_URL
import com.sloy.sevibus.infrastructure.config.ApiConfigurationManager.Companion.DEFAULT_STAGING_URL

internal val SevHostPresets = listOf(
    HostPreset("Dev", DEFAULT_DEBUG_URL),
    HostPreset("Staging", DEFAULT_STAGING_URL),
    HostPreset("Prod", DEFAULT_RELEASE_URL),
)
