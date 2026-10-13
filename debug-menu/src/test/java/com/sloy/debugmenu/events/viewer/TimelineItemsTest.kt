package com.sloy.debugmenu.events.viewer

import com.sloy.debugmenu.events.CapturedEvent
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo

class TimelineItemsTest {

    private fun describe(items: List<ViewerItem>): List<String> = items.map { item ->
        when (item) {
            is EventItem -> "${item.event.name} ${item.delta}"
            is SessionItem -> "[session]"
            is GapItem -> item.label
            is BandHeaderItem -> "<${item.screen} ${item.duration ?: "now"}>"
            is BandEndItem -> "</>"
        }
    }

    @Test
    fun `sample timeline is newest first with deltas gaps and the session card`() {
        val items = describe(timelineItems(ViewerSampleData.events))
        expectThat(items.take(5)).containsExactly(
            "Arrivals Displayed +2m 13s",
            "· 2m 13s in background ·",
            "[session]",
            "For You Viewed +0.02s",
            "Stop Details Closed +2.75s",
        )
        expectThat(items.last()).isEqualTo("App Started first")
        expectThat(items.size).isEqualTo(21)
    }

    @Test
    fun `quiet gap when no session summary lies between`() {
        val events = listOf(
            CapturedEvent("B", timestampMillis = 12_000, id = "b"),
            CapturedEvent("A", timestampMillis = 0, id = "a"),
        )
        expectThat(describe(timelineItems(events))).containsExactly("B +12.00s", "· 12s quiet ·", "A first")
    }

    @Test
    fun `no gap under 10 seconds`() {
        val events = listOf(CapturedEvent("B", timestampMillis = 9_999, id = "b"), CapturedEvent("A", timestampMillis = 0, id = "a"))
        expectThat(timelineItems(events).filterIsInstance<GapItem>()).isEmpty()
    }

    @Test
    fun `sorts by timestamp`() {
        val outOfOrder = listOf(CapturedEvent("A", timestampMillis = 0, id = "a"), CapturedEvent("B", timestampMillis = 500, id = "b"))
        expectThat(describe(timelineItems(outOfOrder))).containsExactly("B +0.50s", "A first")
    }

    @Test
    fun `search keeps matching rows and recomputes gaps between them`() {
        val items = describe(timelineItems(ViewerSampleData.events, query = "stopId=136"))
        expectThat(items).containsExactly(
            "Stop Details Closed +2.75s",
            "Arrivals Displayed +0.61s",
            "Stop Details Viewed +0.02s",
            "Favorite Stop Clicked +1.22s",
        )
    }

    @Test
    fun `no match gives no items`() {
        expectThat(timelineItems(ViewerSampleData.events, query = "nothing like this")).isEmpty()
    }
}
