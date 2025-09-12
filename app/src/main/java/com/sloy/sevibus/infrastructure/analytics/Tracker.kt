package com.sloy.sevibus.infrastructure.analytics

interface Tracker {
    fun track(event: SevEvent)
}