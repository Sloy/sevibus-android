package com.sloy.sevibus.feature.map

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

sealed class ZoomLevel(val minimumLevel: Int) : Comparable<ZoomLevel> {
    data object Close : ZoomLevel(16)
    data object Medium : ZoomLevel(14)
    data object Far : ZoomLevel(13)

    override fun compareTo(other: ZoomLevel): Int {
        return this.minimumLevel.compareTo(other.minimumLevel)
    }

    companion object {
        operator fun invoke(zoomLevel: Int): ZoomLevel {
            return when {
                zoomLevel >= Close.minimumLevel -> Close
                zoomLevel >= Medium.minimumLevel -> Medium
                else -> Far
            }
        }
    }
}

val ZoomLevel.showLineStops: Boolean
    get() = this > ZoomLevel.Far

val ZoomLevel.showAllStops: Boolean
    get() = this > ZoomLevel.Medium

val ZoomLevel.smallStopIcon: Boolean
    get() = this <= ZoomLevel.Medium

val ZoomLevel.lineWidth: Dp
    get() = when (this) {
        is ZoomLevel.Far -> 4.dp
        is ZoomLevel.Medium -> 6.dp
        is ZoomLevel.Close -> 8.dp
    }

val ZoomLevel.stopMarkerSize: Dp
    get() = when (this) {
        is ZoomLevel.Far -> 16.dp
        is ZoomLevel.Medium -> 16.dp
        is ZoomLevel.Close -> 24.dp
    }
