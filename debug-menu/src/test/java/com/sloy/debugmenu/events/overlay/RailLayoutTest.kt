package com.sloy.debugmenu.events.overlay

import com.sloy.debugmenu.events.CapturedEvent
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class RailLayoutTest {

    private fun events(vararg nameAndTime: Pair<String, Long>): List<CapturedEvent> =
        nameAndTime.mapIndexed { index, (name, at) -> CapturedEvent(name, timestampMillis = at, id = "$index") }.asReversed()

    @Test
    fun `markers drift up at 31dp per second`() {
        val marker = railFrame(events("A" to 0), 2_000).markers.single()
        expectThat(marker.y).isEqualTo(62f)
    }

    @Test
    fun `labelled neighbours end up 26dp apart once the newer one made room`() {
        val frame = railFrame(events("A" to 0, "B" to 0), 400)
        expectThat((frame.markers[1].y - frame.markers[0].y).toDouble()).isEqualTo(26.0, 0.001)
    }

    @Test
    fun `a new event makes room gradually before it pops`() {
        val half = railFrame(events("A" to 0, "B" to 0), 60 + 125).markers
        val gap = half[1].y - half[0].y
        expectThat(gap > 0f && gap < 26f).isTrue()
        expectThat(half[0].markerScale).isEqualTo(0f)
        expectThat(half[0].labelVisible).isFalse()
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
    fun `labels hold for 2s then fade to 0_55 at 9s`() {
        expectThat(railLabelAlpha(2f)).isEqualTo(1f)
        expectThat(railLabelAlpha(9f).toDouble()).isEqualTo(0.55, 0.001)
        expectThat(railLabelAlpha(5.5f).toDouble()).isEqualTo(0.775, 0.001)
    }

    @Test
    fun `labels hide after 9s`() {
        expectThat(railFrame(events("A" to 0), 8_900).markers.single().labelVisible).isTrue()
        expectThat(railFrame(events("A" to 0), 9_300).markers.single().labelVisible).isFalse()
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
    fun `events of a burst make room 60ms apart and pop once their room is made`() {
        expectThat(railFrame(events("A" to 1_000, "B" to 1_000), 1_249).markers.map { it.markerScale }).containsExactly(0f, 0f)
        expectThat(railFrame(events("A" to 1_000, "B" to 1_000), 1_300).markers.map { it.markerScale > 0f }).containsExactly(false, true)
        expectThat(railFrame(events("A" to 1_000, "B" to 1_000), 1_400).markers.map { it.markerScale > 0f }).containsExactly(true, true)
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
    fun `demo at 10_9s labels the last 15 events`() {
        val frame = railFrame(OverlayDemo.events(0), 10_900)
        expectThat(frame.markers.count { it.labelVisible }).isEqualTo(15)
        expectThat(frame.markers.size).isEqualTo(17)
    }
}
