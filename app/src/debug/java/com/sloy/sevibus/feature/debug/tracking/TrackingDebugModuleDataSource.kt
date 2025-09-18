package com.sloy.sevibus.feature.debug.tracking

import android.content.Context
import com.sloy.debugmenu.base.DebugModuleDataSource
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json

class TrackingDebugModuleDataSource(context: Context) : DebugModuleDataSource<TrackingDebugModuleState>(context) {

    override val defaultValue
        get() = TrackingDebugModuleState()

    override fun Json.decode(jsonString: String): TrackingDebugModuleState = decodeFromString(jsonString)
    override fun Json.encode(value: TrackingDebugModuleState): String = encodeToString(value)
}

