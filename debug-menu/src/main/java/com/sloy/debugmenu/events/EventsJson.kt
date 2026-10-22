package com.sloy.debugmenu.events

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

internal fun CapturedEvent.toJsonObject(): JsonObject = buildJsonObject {
    put("name", name)
    put("timestamp", timestamp)
    put("timestampMillis", timestampMillis)
    putJsonObject("properties") {
        properties.forEach { (key, value) -> put(key, value) }
    }
}

internal fun List<CapturedEvent>.toSessionJson(): String =
    PrettyJson.encodeToString(JsonArray.serializer(), JsonArray(chronological().map { it.toJsonObject() }))

private val PrettyJson = Json { prettyPrint = true }
