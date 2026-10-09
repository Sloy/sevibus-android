package com.sloy.debugmenu.events

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo

class EventsJsonTest {

    @Test
    fun `exports oldest first with millis and properties`() {
        val newestFirst = listOf(
            CapturedEvent("For You Viewed", mapOf("trigger" to "launch"), timestampMillis = 1_120, id = "2"),
            CapturedEvent("App Started", timestampMillis = 1_050, id = "1"),
        )

        val array = Json.parseToJsonElement(newestFirst.toSessionJson()).jsonArray

        expectThat(array.map { it.jsonObject["name"]!!.jsonPrimitive.content }).containsExactly("App Started", "For You Viewed")
        expectThat(array[0].jsonObject["timestampMillis"]!!.jsonPrimitive.long).isEqualTo(1_050)
        expectThat(array[1].jsonObject["properties"]!!.jsonObject["trigger"]!!.jsonPrimitive.content).isEqualTo("launch")
        expectThat(array[1].jsonObject["timestamp"]!!.jsonPrimitive.content).isEqualTo(1_120L.toClockTime())
    }

    @Test
    fun `empty store exports an empty array`() {
        expectThat(Json.parseToJsonElement(emptyList<CapturedEvent>().toSessionJson()).jsonArray.size).isEqualTo(0)
    }
}
