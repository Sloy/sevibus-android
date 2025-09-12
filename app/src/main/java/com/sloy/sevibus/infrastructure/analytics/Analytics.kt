package com.sloy.sevibus.infrastructure.analytics

class Analytics(private val trackers: List<Tracker>) {
    fun track(event: SevEvent) {
        trackers.forEach { it.track(event) }
    }
}