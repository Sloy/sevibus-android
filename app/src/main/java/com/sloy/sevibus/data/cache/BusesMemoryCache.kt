package com.sloy.sevibus.data.cache

import com.sloy.sevibus.domain.model.Bus
import com.sloy.sevibus.domain.model.RouteId
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class BusesMemoryCache {
    private val cache = ConcurrentHashMap<RouteId, CacheEntry>()

    fun get(routeId: RouteId): List<Bus>? {
        val entry = cache[routeId]
        return if (entry != null && entry.timestamp.elapsedNow() < 10.seconds) {
            entry.data
        } else {
            null
        }
    }

    fun put(stopId: RouteId, data: List<Bus>) {
        cache[stopId] = CacheEntry(data, TimeSource.Monotonic.markNow())
    }

    private data class CacheEntry(val data: List<Bus>, val timestamp: TimeMark)
}
