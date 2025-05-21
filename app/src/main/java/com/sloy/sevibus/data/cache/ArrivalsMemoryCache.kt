package com.sloy.sevibus.data.cache

import com.sloy.sevibus.domain.model.BusArrival
import com.sloy.sevibus.domain.model.StopId
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class ArrivalsMemoryCache {
    private val cache = ConcurrentHashMap<StopId, CacheEntry>()

    fun get(stopId: StopId): List<BusArrival>? {
        val entry = cache[stopId]
        return if (entry != null && entry.timestamp.elapsedNow() < 10.seconds) {
            entry.data
        } else {
            null
        }
    }

    fun put(stopId: StopId, data: List<BusArrival>) {
        cache[stopId] = CacheEntry(data, TimeSource.Monotonic.markNow())
    }

    private data class CacheEntry(val data: List<BusArrival>, val timestamp: TimeMark)
}
