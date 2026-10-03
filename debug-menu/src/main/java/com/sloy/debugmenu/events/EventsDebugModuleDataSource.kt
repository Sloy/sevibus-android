package com.sloy.debugmenu.events

import android.content.Context
import com.sloy.debugmenu.base.DebugModuleDataSource
import kotlinx.serialization.json.Json

class EventsDebugModuleDataSource(context: Context) : DebugModuleDataSource<EventsDebugModuleState>(context) {
    override val defaultValue: EventsDebugModuleState
        get() = EventsDebugModuleState()

    override fun Json.decode(jsonString: String): EventsDebugModuleState = decodeFromString(jsonString)
    override fun Json.encode(value: EventsDebugModuleState): String = encodeToString(value)
}
