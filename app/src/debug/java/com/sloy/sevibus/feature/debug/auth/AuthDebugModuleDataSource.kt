package com.sloy.sevibus.feature.debug.auth

import android.content.Context
import com.sloy.debugmenu.base.DebugModuleDataSource
import kotlinx.serialization.json.Json

class AuthDebugModuleDataSource(context: Context) : DebugModuleDataSource<AuthDebugModuleState>(context) {

    override val defaultValue
        get() = AuthDebugModuleState()

    override fun Json.decode(jsonString: String): AuthDebugModuleState = decodeFromString(jsonString)
    override fun Json.encode(value: AuthDebugModuleState): String = encodeToString(value)
}