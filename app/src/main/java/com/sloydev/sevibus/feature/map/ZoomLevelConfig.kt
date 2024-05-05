package com.sloydev.sevibus.feature.map

object ZoomLevelConfig {
    fun showLineStops(zoomLevel: Int): Boolean{
        return zoomLevel > 12
    }
    fun showSparseStops(zoomLevel: Int): Boolean {
        return zoomLevel > 15
    }
    fun showOtherStopsInLine(zoomLevel: Int): Boolean {
        return zoomLevel > 15
    }
}
