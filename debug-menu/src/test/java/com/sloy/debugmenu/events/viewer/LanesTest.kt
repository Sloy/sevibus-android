package com.sloy.debugmenu.events.viewer

import com.sloy.debugmenu.events.CapturedEvent
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class LanesTest {
    private val start = ViewerSampleData.startMillis

    @Test
    fun `range and span come from the visible items`() {
        val model = lanesModel(ViewerSampleData.events, start + 1_050, start + 15_000)
        expectThat(model.range).isEqualTo("16:21:01 → 16:21:15")
        expectThat(model.span).isEqualTo("14s")
    }

    @Test
    fun `window is padded 5 percent on each side`() {
        val events = listOf(CapturedEvent("A", timestampMillis = 10_000, id = "a"), CapturedEvent("B", timestampMillis = 0, id = "b"))
        val model = lanesModel(events, 0, 10_000)
        expectThat(model.events.map { it.key }).containsExactly("b", "a")
        expectThat(model.events[0].x.toDouble()).isEqualTo(0.05 / 1.1, 0.0001)
        expectThat(model.events[1].x.toDouble()).isEqualTo(1.05 / 1.1, 0.0001)
    }

    @Test
    fun `window is at least 5 seconds`() {
        val events = listOf(CapturedEvent("A", timestampMillis = 1_000, id = "a"))
        val model = lanesModel(events, 1_000, 1_000)
        expectThat(model.events.single().x.toDouble()).isEqualTo(0.5, 0.0001)
    }

    @Test
    fun `screen bars run from a view to the next view or session summary`() {
        val model = lanesModel(ViewerSampleData.events, start + 1_050, start + 15_000)
        expectThat(model.screens.map { it.label }).containsExactly("For You", "Lines", "Stop Details", "For You", "Stop Details", "For You")
        expectThat(model.screens.last().end).isEqualTo(model.events.first { it.key == ViewerSampleData.sessionSummaryId }.x)
    }

    @Test
    fun `screen resumed after a session summary has a bar from its first event`() {
        val model = lanesModel(ViewerSampleData.events, start + 140_000, start + 145_300)
        val resumed = model.screens.single()
        expectThat(resumed.label).isEqualTo("For You (resumed)")
        expectThat(resumed.key).isEqualTo(ViewerSampleData.id(19))
        expectThat(resumed.start).isEqualTo(model.events.single { it.key == ViewerSampleData.id(19) }.x)
    }

    @Test
    fun `screen still open runs to the end of the window`() {
        val events = listOf(
            CapturedEvent("For You Viewed", timestampMillis = 0, id = "view"),
            CapturedEvent("Arrivals Displayed", timestampMillis = 10_000, id = "arrivals"),
        )
        val model = lanesModel(events, 0, 10_000)
        expectThat(model.screens.single().end).isEqualTo(1f)
    }

    @Test
    fun `clicks and other events go to their lanes`() {
        val model = lanesModel(ViewerSampleData.events, start + 1_050, start + 15_000)
        expectThat(model.clicks.size).isEqualTo(2)
        expectThat(model.events.size).isEqualTo(11)
    }

    @Test
    fun `axis has five labels`() {
        expectThat(lanesModel(ViewerSampleData.events, start + 1_050, start + 15_000).axis.size).isEqualTo(5)
    }

    @Test
    fun `tap hits the nearest mark within tolerance`() {
        val model = LanesModel("", "", emptyList(), listOf(LaneMark("a", 0.2f), LaneMark("b", 0.3f)), emptyList(), emptyList())
        expectThat(model.markAt(Lane.CLICKS, 0.27f, 0.05f)).isEqualTo("b")
        expectThat(model.markAt(Lane.CLICKS, 0.6f, 0.05f)).isNull()
    }

    @Test
    fun `tap on a screen bar hits its view`() {
        val model = LanesModel("", "", listOf(LaneBar("v", 0.1f, 0.4f, "Lines")), emptyList(), emptyList(), emptyList())
        expectThat(model.markAt(Lane.SCREENS, 0.3f, 0.05f)).isEqualTo("v")
    }
}
