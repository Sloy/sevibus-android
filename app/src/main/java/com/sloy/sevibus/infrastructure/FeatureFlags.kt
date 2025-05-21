package com.sloy.sevibus.infrastructure

object FeatureFlags {
    val bonobus = true
    val favoritesRequireUser = true
    val showCardUpdateWarning = true
    val showMapDebugInfo = BuildVariant.isDebug()
    val showLinesOverview = true
}
