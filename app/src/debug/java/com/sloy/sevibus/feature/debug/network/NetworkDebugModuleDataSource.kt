package com.sloy.sevibus.modules.tracking

import android.content.Context
import com.sloy.debugmenu.base.DebugModuleDataSource
import kotlinx.serialization.json.Json

class NetworkDebugModuleDataSource(context: Context) : DebugModuleDataSource<NetworkDebugModuleState>(context) {
    override val defaultValue
        get() = NetworkDebugModuleState()

    override fun Json.decode(jsonString: String): NetworkDebugModuleState = decodeFromString(jsonString)
    override fun Json.encode(value: NetworkDebugModuleState): String = encodeToString(value)
}
