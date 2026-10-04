package com.sloy.debugmenu.events

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import java.util.UUID

/**
 * Analytics event captured for debugging purposes.
 */
data class CapturedEvent(
    val name: String,
    val properties: Map<String, String> = emptyMap(),
    val timestamp: String,
    val id: String = UUID.randomUUID().toString(),
)

internal fun CapturedEvent.toPrettyJson(): String {
    val json = buildJsonObject {
        put("name", name)
        put("timestamp", timestamp)
        putJsonObject("properties") {
            properties.forEach { (key, value) -> put(key, value) }
        }
    }
    return PrettyJson.encodeToString(JsonObject.serializer(), json)
}

private val PrettyJson = Json { prettyPrint = true }
