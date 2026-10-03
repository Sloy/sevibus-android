package com.sloy.debugmenu.events

import kotlinx.serialization.Serializable

@Serializable
data class EventsDebugModuleState(
    val isOverlayEnabled: Boolean = false,
)
