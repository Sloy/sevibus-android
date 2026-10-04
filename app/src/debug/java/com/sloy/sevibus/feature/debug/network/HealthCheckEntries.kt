package com.sloy.sevibus.feature.debug.network

import com.sloy.sevibus.data.api.model.HealthCheckDto

/**
 * Label/value pairs shown by the network debug module, in the same order as the Settings screen.
 */
fun HealthCheckDto.toDebugEntries(): Map<String, String> = listOfNotNull(
    host?.let { "Host" to it },
    environment?.let { "Environment" to it },
    database?.let { "Database" to it },
    provider?.let { "Provider" to it },
    version?.let { "Version" to it },
    clientVersion?.let { "Client version" to it },
    ip?.let { "IP" to it },
    timestamp?.let { "Timestamp" to it },
    uptime?.let { "Uptime" to "${it}s" },
).toMap()
