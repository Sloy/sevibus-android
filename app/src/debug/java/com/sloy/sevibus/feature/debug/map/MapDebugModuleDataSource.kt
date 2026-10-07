package com.sloy.sevibus.feature.debug.map

import android.content.Context
import com.sloy.debugmenu.base.DebugModuleDataSource
import kotlinx.serialization.json.Json

class MapDebugModuleDataSource(context: Context) : DebugModuleDataSource<MapDebugModuleState>(context) {
    override val defaultValue
        get() = MapDebugModuleState()

    override fun Json.decode(jsonString: String): MapDebugModuleState = decodeFromString(jsonString)
    override fun Json.encode(value: MapDebugModuleState): String = encodeToString(value)
}
