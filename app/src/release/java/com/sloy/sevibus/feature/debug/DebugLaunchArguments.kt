package com.sloy.sevibus.feature.debug

import android.content.Intent

object DebugLaunchArguments {
    const val EXTRA_API_HOST = "debugApiHost"

    val mapMode: DebugMapMode = DebugMapMode.Full

    fun apply(intent: Intent) = Unit
}
