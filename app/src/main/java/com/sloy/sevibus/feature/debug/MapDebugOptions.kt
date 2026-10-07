package com.sloy.sevibus.feature.debug

/**
 * What the home map draws, set from the debug menu. Release builds always use the defaults.
 */
data class MapDebugOptions(
    val showMapState: Boolean = false,
    val showCamera: Boolean = false,
    val showVisibleArea: Boolean = false,
    val showFitBounds: Boolean = false,
    val hideStops: Boolean = false,
    val hideBuses: Boolean = false,
)
