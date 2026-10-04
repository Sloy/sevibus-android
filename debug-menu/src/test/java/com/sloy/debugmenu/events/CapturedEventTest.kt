package com.sloy.debugmenu.events

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo

class CapturedEventTest {

    @Test
    fun `pretty json contains name timestamp and properties`() {
        val event = CapturedEvent(name = "Add Favorite Clicked", properties = mapOf("stopId" to "42"), timestamp = "10:15:30")

        val text = event.toPrettyJson()
        val json = Json.parseToJsonElement(text).jsonObject

        expectThat(json["name"]?.jsonPrimitive?.content).isEqualTo("Add Favorite Clicked")
        expectThat(json["timestamp"]?.jsonPrimitive?.content).isEqualTo("10:15:30")
        expectThat(json["properties"]?.jsonObject?.get("stopId")?.jsonPrimitive?.content).isEqualTo("42")
        expectThat(text).contains("\n")
    }
}
