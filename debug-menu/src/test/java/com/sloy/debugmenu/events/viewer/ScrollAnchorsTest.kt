package com.sloy.debugmenu.events.viewer

import com.sloy.debugmenu.events.CapturedEvent
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo

class ScrollAnchorsTest {

    private fun item(at: Long) = EventItem(CapturedEvent("E$at", timestampMillis = at, id = "$at"), delta = "")

    @Test
    fun `rows anchor at their time and other items at the next row below`() {
        val items = listOf(BandHeaderItem("h", "Lines", 900, null), item(3_000), GapItem("g", "quiet"), item(1_000), BandEndItem("e"))
        expectThat(scrollAnchors(items)).containsExactly(3_000L, 3_000L, 1_000L, 1_000L, 1_000L)
    }

    @Test
    fun `time inside an item runs from its anchor to the next one`() {
        val anchors = listOf(3_000L, 1_000L, 500L)
        expectThat(anchorTime(anchors, 0, 0.25f)).isEqualTo(2_500L)
        expectThat(anchorTime(anchors, 2, 0.5f)).isEqualTo(500L)
    }

    @Test
    fun `a time maps back to the item and fraction showing it`() {
        val anchors = listOf(3_000L, 3_000L, 1_000L, 500L)
        expectThat(anchorPosition(anchors, 2_500)).isEqualTo(1 to 0.25f)
        expectThat(anchorPosition(anchors, 3_000)).isEqualTo(0 to 0f)
        expectThat(anchorPosition(anchors, 9_000)).isEqualTo(0 to 0f)
        expectThat(anchorPosition(anchors, 100)).isEqualTo(3 to 0f)
    }
}
