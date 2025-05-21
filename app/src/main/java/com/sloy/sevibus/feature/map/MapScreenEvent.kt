package com.sloy.sevibus.feature.map

interface MapScreenEvent {
    data class Error(val message: String) : MapScreenEvent
}
