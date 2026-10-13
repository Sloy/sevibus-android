package com.sloy.debugmenu.events

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.first
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo

class EventStoreTest {

    @Test
    fun `keeps newest events first`() {
        val store = EventStore()

        store.add(event("first"))
        store.add(event("second"))

        expectThat(store.events.value.map { it.name }).containsExactly("second", "first")
    }

    @Test
    fun `drops the oldest events beyond capacity`() {
        val store = EventStore(capacity = 3)

        (1..5).forEach { store.add(event("event $it")) }

        expectThat(store.events.value).hasSize(3)
        expectThat(store.events.value.map { it.name }).containsExactly("event 5", "event 4", "event 3")
    }

    @Test
    fun `default capacity is 200`() {
        val store = EventStore()

        (1..250).forEach { store.add(event("event $it")) }

        expectThat(store.events.value).hasSize(200)
        expectThat(store.events.value).first().get { name }.isEqualTo("event 250")
    }

    @Test
    fun `clear removes all events`() {
        val store = EventStore()
        store.add(event("first"))

        store.clear()

        expectThat(store.events.value).isEmpty()
    }

    private fun event(name: String) = CapturedEvent(name = name, timestampMillis = 0)
}
