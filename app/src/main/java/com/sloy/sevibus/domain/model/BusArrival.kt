package com.sloy.sevibus.domain.model

data class BusArrival(
    val bus: BusId,
    val distance: Int,
    val seconds: Int?,
    val line: LineSummary,
    val route: Route
) : Comparable<BusArrival> {
    val minutes: Int?
        get() = seconds?.let { it / 60 }

    override fun compareTo(other: BusArrival): Int {
        val o1 = this
        val o2 = other
        return when {
            o1.seconds == null && o2.seconds == null -> 0 // both are null
            o1.seconds == null -> 1 // o1 is null, o2 is not
            o2.seconds == null -> -1 // o1 is not null, o2 is
            else -> o1.seconds.compareTo(o2.seconds) // both are not null, compare normally
        }
    }
}

