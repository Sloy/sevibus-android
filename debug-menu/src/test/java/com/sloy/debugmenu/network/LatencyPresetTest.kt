package com.sloy.debugmenu.network

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThanOrEqualTo
import strikt.assertions.isLessThanOrEqualTo
import kotlin.random.Random

class LatencyPresetTest {

    @Test
    fun `off has no delay`() {
        expectThat(LatencyPreset.Off.nextDelayMs(Random(1))).isEqualTo(0L)
    }

    @Test
    fun `3G delay stays within 10 percent of the center`() {
        val random = Random(42)
        repeat(200) {
            expectThat(LatencyPreset.Standard3G.nextDelayMs(random)).isGreaterThanOrEqualTo(505L).isLessThanOrEqualTo(618L)
        }
    }

    @Test
    fun `slow 3G delay stays within 10 percent of the center`() {
        val random = Random(42)
        repeat(200) {
            expectThat(LatencyPreset.Slow3G.nextDelayMs(random)).isGreaterThanOrEqualTo(1800L).isLessThanOrEqualTo(2200L)
        }
    }
}
