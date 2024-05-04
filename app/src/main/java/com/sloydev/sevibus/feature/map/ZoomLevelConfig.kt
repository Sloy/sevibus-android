package com.sloydev.sevibus.feature.map

object ZoomLevelConfig {
    fun isLineStopsVisible(zoomLevel: Int): Boolean{
        return zoomLevel > 12
    }
    fun isSparseStopsVisible(zoomLevel: Int): Boolean {
        return zoomLevel > 15
    }
}
