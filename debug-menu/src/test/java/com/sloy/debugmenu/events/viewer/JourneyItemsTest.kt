package com.sloy.debugmenu.events.viewer

import com.sloy.debugmenu.events.CapturedEvent
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo

class JourneyItemsTest {

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
    fun `sample journey groups by screen newest first with the view as last row`() {
        expectThat(describe(journeyItems(ViewerSampleData.events))).containsExactly(
            "<For You (resumed) now>",
            "Arrivals Displayed +2m 13s",
            "</>",
            "· 2m 13s in background ·",
            "[session]",
            "<For You 2.3s>", "For You Viewed +0.02s", "</>",
            "<Stop Details 3.4s>", "Stop Details Closed +2.75s", "Arrivals Displayed +0.61s", "Stop Details Viewed +0.02s", "</>",
            "<For You 1.8s>", "Favorite Stop Clicked +1.22s", "Arrivals Displayed +0.60s", "For You Viewed +0.02s", "</>",
            "<Stop Details 3.2s>", "Stop Details Closed +2.57s", "Bottom Sheet Changed +0.01s", "Arrivals Displayed +0.65s", "Stop Details Viewed +0.01s", "</>",
            "<Lines 1.9s>", "Map Stop Clicked +0.64s", "Map Explored +0.75s", "Line Paths Displayed +0.45s", "Lines Viewed +0.49s", "</>",
            "<For You 1.3s>", "Arrivals Displayed +0.79s", "For You Viewed +0.07s", "</>",
            "App Started first",
        )
    }

    @Test
    fun `rows in a band are marked in band`() {
        expectThat(journeyItems(ViewerSampleData.events).filterIsInstance<EventItem>().count { !it.inBand }).isEqualTo(1)
    }

    @Test
    fun `events before any view are loose rows`() {
        val events = listOf(CapturedEvent("Map Explored", timestampMillis = 10, id = "b"), CapturedEvent("App Started", timestampMillis = 0, id = "a"))
        expectThat(describe(journeyItems(events))).containsExactly("Map Explored +0.01s", "App Started first")
    }

    @Test
    fun `resumed band without lastScreen`() {
        val events = listOf(
            CapturedEvent("Map Explored", timestampMillis = 20_000, id = "c"),
            CapturedEvent("Session Summary", timestampMillis = 10_000, id = "b"),
            CapturedEvent("App Started", timestampMillis = 0, id = "a"),
        )
        expectThat(describe(journeyItems(events)).first()).isEqualTo("<Resumed now>")
    }

    @Test
    fun `search hides bands without matching rows`() {
        val items = describe(journeyItems(ViewerSampleData.events, query = "stopId=412"))
        expectThat(items).containsExactly(
            "<Stop Details 3.2s>", "Stop Details Closed +2.57s", "Arrivals Displayed +0.65s", "Stop Details Viewed +0.01s", "</>",
            "<Lines 1.9s>", "Map Stop Clicked +0.64s", "</>",
        )
    }
}
