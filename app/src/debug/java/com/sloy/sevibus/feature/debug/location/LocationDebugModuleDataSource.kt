package com.sloy.sevibus.feature.debug.location

import android.content.Context
import com.sloy.debugmenu.base.DebugModuleDataSource
import kotlinx.serialization.json.Json

class LocationDebugModuleDataSource(context: Context) : DebugModuleDataSource<LocationDebugModuleState>(context) {
    override val defaultValue
        get() = LocationDebugModuleState()

    override fun Json.decode(jsonString: String): LocationDebugModuleState = decodeFromString(jsonString)
    override fun Json.encode(value: LocationDebugModuleState): String = encodeToString(value)
}
