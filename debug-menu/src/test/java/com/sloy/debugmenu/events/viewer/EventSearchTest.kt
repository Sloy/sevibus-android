package com.sloy.debugmenu.events.viewer

import com.sloy.debugmenu.events.CapturedEvent
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class EventSearchTest {

    private val event = CapturedEvent("Map Stop Clicked", mapOf("stopId" to "412", "source" to "map"), timestampMillis = 0)

    @Test
    fun `empty query matches everything`() {
        expectThat(event.matches("  ")).isTrue()
    }

    @Test
    fun `matches the name ignoring case`() {
        expectThat(event.matches("stop clicked")).isTrue()
    }

    @Test
    fun `matches a key`() {
        expectThat(event.matches("STOPID")).isTrue()
    }

    @Test
    fun `matches a value`() {
        expectThat(event.matches("412")).isTrue()
    }

    @Test
    fun `matches key equals value`() {
        expectThat(event.matches("stopId=412")).isTrue()
    }

    @Test
    fun `matches key space value`() {
        expectThat(event.matches("source map")).isTrue()
    }

    @Test
    fun `does not match other text`() {
        expectThat(event.matches("favorites")).isFalse()
    }

    @Test
    fun `key and value of different properties do not match together`() {
        expectThat(event.matches("stopId=map")).isFalse()
    }
}
