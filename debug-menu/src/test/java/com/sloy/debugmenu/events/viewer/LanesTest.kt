package com.sloy.debugmenu.events.viewer

import com.sloy.debugmenu.events.CapturedEvent
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class LanesTest {
    private val start = ViewerSampleData.startMillis
    private val strip = lanesStrip(ViewerSampleData.events)

    private fun event(name: String, at: Long, id: String = name) = CapturedEvent(name, timestampMillis = at, id = id)

    @Test
    fun `time runs left to right at 24dp per second after the edge`() {
        val strip = lanesStrip(listOf(event("B", 2_000), event("A", 0)))
        expectThat(strip.xAt(0)).isEqualTo(LanesSpec.EDGE)
        expectThat(strip.xAt(1_000)).isEqualTo(LanesSpec.EDGE + 24f)
        expectThat(strip.width).isEqualTo(LanesSpec.EDGE + 48f + LanesSpec.EDGE)
    }

    @Test
    fun `gaps over 10s collapse to a fixed width break`() {
        val strip = lanesStrip(listOf(event("B", 60_000), event("A", 0)))
        expectThat(strip.xAt(60_000)).isEqualTo(LanesSpec.EDGE + LanesSpec.BREAK_WIDTH)
        expectThat(strip.breaks.single().label).isEqualTo("1m 0s")
        expectThat(strip.breaks.single().start).isEqualTo(LanesSpec.EDGE)
    }

    @Test
    fun `time at a position inverts the position of a time`() {
        listOf(start + 1_050, start + 4_254, start + 15_000, start + 80_000, start + 145_300).forEach { time ->
            expectThat(strip.timeAt(strip.xAt(time))).isEqualTo(time)
        }
    }

    @Test
    fun `screen bars run from a view to the next view or session summary`() {
        expectThat(strip.screens.map { it.label }).containsExactly(
            "For You", "Lines", "Stop Details", "For You", "Stop Details", "For You", "For You (resumed)",
        )
        expectThat(strip.screens[5].end).isEqualTo(strip.xAt(start + 15_000))
    }

    @Test
    fun `the open screen runs to the end of the strip`() {
        val resumed = strip.screens.last()
        expectThat(resumed.key).isEqualTo(ViewerSampleData.id(19))
        expectThat(resumed.start).isEqualTo(strip.xAt(start + 145_300))
        expectThat(resumed.end).isEqualTo(strip.width)
    }

    @Test
    fun `clicks and other events go to their lanes`() {
        expectThat(strip.clicks.size).isEqualTo(2)
        expectThat(strip.events.size).isEqualTo(12)
    }

    @Test
    fun `ticks every 5 seconds of clock time inside each stretch`() {
        expectThat(strip.ticks.map { it.label }).containsExactly("21:05", "21:10", "21:15")
        expectThat(strip.ticks.first().x).isEqualTo(strip.xAt(start + 5_000))
    }

    @Test
    fun `tap hits the nearest mark within tolerance`() {
        val strip = LanesStrip(clicks = listOf(LaneMark("a", 20f), LaneMark("b", 30f)))
        expectThat(strip.markAt(Lane.CLICKS, 27f, 5f)).isEqualTo("b")
        expectThat(strip.markAt(Lane.CLICKS, 60f, 5f)).isNull()
    }

    @Test
    fun `tap on a screen bar hits its first event`() {
        val strip = LanesStrip(screens = listOf(LaneBar("v", 10f, 40f, "Lines")))
        expectThat(strip.markAt(Lane.SCREENS, 30f, 5f)).isEqualTo("v")
    }

    @Test
    fun `bar labels shrink to initials when the name does not fit`() {
        expectThat(barLabel("Line Stops", 60f)).isEqualTo("Line Stops")
        expectThat(barLabel("Line Stops", 30f)).isEqualTo("LS")
        expectThat(barLabel("Lines", 30f)).isEqualTo("Li")
        expectThat(barLabel("Line Stops", 16f)).isEqualTo("L")
        expectThat(barLabel("Line Stops", 8f)).isNull()
    }
}
