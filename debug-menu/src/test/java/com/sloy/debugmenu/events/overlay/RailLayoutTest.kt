package com.sloy.debugmenu.events.overlay

import com.sloy.debugmenu.events.CapturedEvent
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo

class RailLayoutTest {

    private fun events(vararg nameAndTime: Pair<String, Long>): List<CapturedEvent> =
        nameAndTime.mapIndexed { index, (name, at) -> CapturedEvent(name, timestampMillis = at, id = "$index") }.asReversed()

    @Test
    fun `markers drift up at 31dp per second`() {
        val marker = railFrame(events("A" to 0), 2_000).markers.single()
        expectThat(marker.y).isEqualTo(62f)
        expectThat(marker.trueY).isEqualTo(62f)
    }

    @Test
    fun `labelled neighbours are pushed 26dp apart`() {
        val frame = railFrame(events("A" to 1_000, "B" to 1_000), 1_000)
        expectThat(frame.markers.map { it.y }).containsExactly(0f, 26f)
    }

    @Test
    fun `unlabelled neighbours are pushed 7dp apart`() {
        val frame = railFrame(events("A" to 0, "B" to 0), 10_000)
        expectThat(frame.markers.map { it.y }).containsExactly(310f, 317f)
    }

    @Test
    fun `spread events keep their true position`() {
        val frame = railFrame(events("A" to 0, "B" to 2_000), 2_000)
        expectThat(frame.markers.map { it.y }).containsExactly(0f, 62f)
    }

    @Test
    fun `markers less than 150ms apart are linked to the newer one`() {
        val frame = railFrame(events("A" to 0, "B" to 149, "C" to 2_000), 2_000)
        expectThat(frame.markers.map { it.name to (it.linkLength > 0f) }).containsExactly("C" to false, "B" to false, "A" to true)
        expectThat(frame.markers.last().linkLength.toDouble()).isEqualTo(26.0, 0.001)
    }

    @Test
    fun `labels hold for 2s then fade to 0_55 until 9s`() {
        expectThat(railLabelAlpha(2f)).isEqualTo(1f)
        expectThat(railLabelAlpha(9f)).isEqualTo(0f)
        expectThat(railLabelAlpha(8.99f).toDouble()).isEqualTo(0.5506, 0.001)
        expectThat(railLabelAlpha(5.5f).toDouble()).isEqualTo(0.775, 0.001)
    }

    @Test
    fun `markers fade over the top 90dp`() {
        val nearTop = railFrame(events("A" to 0), 16_000).markers.single()
        expectThat(nearTop.markerAlpha.toDouble()).isEqualTo((540.0 - 496.0) / 90.0, 0.001)
    }

    @Test
    fun `events are removed 1_5s after leaving the rail`() {
        val window = (540f / 31f * 1000).toLong() + 1_500
        expectThat(railFrame(events("A" to 0), window).markers.size).isEqualTo(1)
        expectThat(railFrame(events("A" to 0), window + 1).markers).isEmpty()
    }

    @Test
    fun `markers of a burst pop 60ms apart after 40ms`() {
        val frame = railFrame(events("A" to 1_000, "B" to 1_000, "C" to 5_000), 5_000)
        expectThat(frame.markers.map { it.popAt }).containsExactly(5_040L, 1_100L, 1_040L)
    }

    @Test
    fun `a view opens a screen band until the next view`() {
        val frame = railFrame(events("App Started" to 0, "Lines Viewed" to 1_000, "Map Explored" to 2_000, "For You Viewed" to 3_000), 3_000)
        expectThat(frame.bands.map { it.top to it.bottom }).containsExactly(
            (62f + 10f) to (31f - 10f),
            (0f + 10f) to (0f - 10f),
        )
    }

    @Test
    fun `ticks every 5 seconds inside the rail`() {
        expectThat(railFrame(emptyList(), 0).ticks).containsExactly(RailTick(155f, "5s"), RailTick(310f, "10s"), RailTick(465f, "15s"))
    }

    @Test
    fun `demo at 10_6s labels the last 15 events`() {
        val frame = railFrame(OverlayDemo.events(0), 10_600)
        expectThat(frame.markers.count { it.labelVisible }).isEqualTo(15)
        expectThat(frame.markers.size).isEqualTo(17)
    }
}
