package com.sloy.debugmenu.network

import kotlinx.serialization.Serializable
import kotlin.random.Random

/**
 * Simulated network latency. Each request waits [centerMs] with a ±10% jitter.
 */
@Serializable
enum class LatencyPreset(val label: String, val centerMs: Int) {
    Off("Off", 0),
    Standard3G("3G", 562),
    Slow3G("Slow 3G", 2000);

    fun nextDelayMs(random: Random = Random.Default): Long {
        if (centerMs == 0) return 0L
        val min = (centerMs * 0.9).toLong()
        val maxExclusive = (centerMs * 1.1).toLong() + 1L
        return random.nextLong(min, maxExclusive)
    }
}
