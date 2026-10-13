package com.sloy.debugmenu.events.overlay

import com.sloy.debugmenu.events.CapturedEvent
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import strikt.assertions.single

class StackLayoutTest {

    private val tall = 560f

    private fun events(vararg nameAndTime: Pair<String, Long>): List<CapturedEvent> =
        nameAndTime.mapIndexed { index, (name, at) -> CapturedEvent(name, timestampMillis = at, id = "$index") }.asReversed()

    @Test
    fun `empty events give an empty frame`() {
        expectThat(stackFrame(emptyList(), 1_000, tall)).isEqualTo(StackFrame(emptyList(), 0, -34f))
    }

    @Test
    fun `a fresh chip sits at the bottom fully visible`() {
        val chip = stackFrame(events("Lines Viewed" to 0), 0, tall).chips.single()
        expectThat(chip.y).isEqualTo(0f)
        expectThat(chip.alpha).isEqualTo(1f)
        expectThat(chip.timerFraction).isEqualTo(1f)
        expectThat(chip.phase).isEqualTo(ChipPhase.IN)
        expectThat(chip.foldIndex).isNull()
    }

    @Test
    fun `newest is at the bottom and separate events are 9dp apart`() {
        val frame = stackFrame(events("A" to 0, "B" to 1_000), 1_000, tall)
        expectThat(frame.chips.map { it.name to it.y }).containsExactly("B" to 0f, "A" to -37f)
    }

    @Test
    fun `events within 150ms are a burst 3dp apart`() {
        val frame = stackFrame(events("A" to 0, "B" to 150), 1_000, tall)
        expectThat(frame.chips.map { it.y }).containsExactly(0f, -31f)
    }

    @Test
    fun `same timestamp events enter 45ms apart`() {
        val batch = events("A" to 1_000, "B" to 1_000)
        expectThat(stackFrame(batch, 1_044, tall).chips.map { it.name }).containsExactly("A")
        expectThat(stackFrame(batch, 1_045, tall).chips.map { it.name }).containsExactly("B", "A")
    }

    @Test
    fun `future events are not shown yet`() {
        expectThat(stackFrame(events("A" to 2_000), 1_000, tall).chips).isEmpty()
    }

    @Test
    fun `repeating the newest name within 3s coalesces and restarts the lifetime`() {
        val frame = stackFrame(events("A" to 0, "A" to 2_000), 2_000, tall)
        expectThat(frame.chips).single().and {
            get { count }.isEqualTo(2)
            get { timerFraction }.isEqualTo(1f)
            get { alpha }.isEqualTo(1f)
        }
    }

    @Test
    fun `repeating after 3s gives a new chip`() {
        expectThat(stackFrame(events("A" to 0, "A" to 3_000), 3_000, tall).chips.map { it.count }).containsExactly(1, 1)
    }

    @Test
    fun `only the newest chip coalesces`() {
        val frame = stackFrame(events("A" to 0, "B" to 500, "A" to 1_000), 1_000, tall)
        expectThat(frame.chips.map { it.name }).containsExactly("A", "B", "A")
    }

    @Test
    fun `alpha holds for 2s then fades to half at the end of the lifetime`() {
        expectThat(stackAlpha(2_000, 5_000)).isEqualTo(1f)
        expectThat(stackAlpha(3_500, 5_000)).isEqualTo(0.75f)
        expectThat(stackAlpha(5_000, 5_000)).isEqualTo(0.5f)
    }

    @Test
    fun `timer drains over the lifetime`() {
        expectThat(stackFrame(events("A" to 0), 2_500, tall).chips.single().timerFraction).isEqualTo(0.5f)
    }

    @Test
    fun `a chip leaves after its lifetime and is removed 450ms later`() {
        val batch = events("A" to 0)
        expectThat(stackFrame(batch, 5_001, tall).chips.single()).and {
            get { phase }.isEqualTo(ChipPhase.OUT)
            get { alpha }.isEqualTo(0f)
        }
        expectThat(stackFrame(batch, 5_451, tall).chips).isEmpty()
    }

    @Test
    fun `chips beyond the available height fold into a pile`() {
        val batch = events("A" to 0, "B" to 1_000, "C" to 2_000, "D" to 3_000, "E" to 4_000)
        val frame = stackFrame(batch, 4_000, maxHeight = 28f + 37f + 37f)
        expectThat(frame.chips.map { it.name to it.foldIndex }).containsExactly("E" to null, "D" to null, "C" to null, "B" to 0, "A" to 1)
        expectThat(frame.chips[3]).and {
            get { y }.isEqualTo(-74f - 5f)
            get { scale }.isEqualTo(0.94f)
            get { alpha }.isEqualTo(0.40f)
        }
        expectThat(frame.chips[4]).and {
            get { y }.isEqualTo(-74f - 8f)
            get { scale }.isEqualTo(0.91f)
            get { alpha }.isEqualTo(0.28f)
        }
        expectThat(frame.olderCount).isEqualTo(2)
        expectThat(frame.olderY).isEqualTo(-74f - 34f)
    }

    @Test
    fun `only three folded chips peek out`() {
        val batch = events(*Array(6) { "E$it" to it * 1_000L })
        val frame = stackFrame(batch, 5_000, maxHeight = 28f)
        expectThat(frame.chips.drop(1).map { it.alpha }).containsExactly(0.40f, 0.28f, 0.16f, 0f, 0f)
        expectThat(frame.chips.drop(1).map { it.y }).containsExactly(-5f, -8f, -11f, -11f, -11f)
    }

    @Test
    fun `demo at 10_6s matches the reference stack`() {
        val frame = stackFrame(OverlayDemo.events(0), 10_600, tall)
        expectThat(frame.chips.map { it.name to it.count }).containsExactly(
            "Arrivals Displayed" to 3,
            "For You Viewed" to 1,
            "Edit Favorites Cancelled" to 1,
            "Edit Favorites Viewed" to 1,
            "Edit Favorites Clicked" to 1,
            "Stop Details Closed" to 1,
            "Bottom Sheet Changed" to 1,
            "Arrivals Displayed" to 2,
        )
        expectThat(frame.chips.map { it.y }).containsExactly(0f, -31f, -62f, -99f, -130f, -167f, -198f, -235f)
    }
}
