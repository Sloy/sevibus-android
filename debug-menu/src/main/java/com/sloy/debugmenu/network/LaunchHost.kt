package com.sloy.debugmenu.network

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * Applies an API host passed when launching the app, for example by end-to-end tests.
 * Missing or invalid hosts leave the current override untouched.
 */
fun NetworkDebugModuleDataSource.applyLaunchHost(host: String?) {
    val url = host?.trim()?.toHttpUrlOrNull() ?: return
    val origin = url.newBuilder().encodedPath("/").query(null).fragment(null).build().toString().trimEnd('/')
    updateState(getCurrentState().copy(hostOverride = origin))
}
