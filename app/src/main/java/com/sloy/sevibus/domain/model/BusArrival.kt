package com.sloy.sevibus.domain.model

import kotlin.math.round


sealed class BusArrival(
    open val line: LineSummary,
    open val route: Route,
) : Comparable<BusArrival> {

    override fun compareTo(other: BusArrival): Int {
        return when {
            this is Available.Arriving && other !is Available.Arriving -> -1
            this !is Available.Arriving && other is Available.Arriving -> 1
            this is Available.Estimation && other is Available.Estimation -> this.seconds.compareTo(other.seconds)
            this is Available.Estimation && other !is Available.Estimation -> -1
            this !is Available.Estimation && other is Available.Estimation -> 1
            this is Available.NoEstimation && other !is Available.NoEstimation -> -1
            this !is Available.NoEstimation && other is Available.NoEstimation -> 1
            this is NotAvailable && other !is NotAvailable -> 1
            this !is NotAvailable && other is NotAvailable -> -1
            else -> 0
        }
    }

    data class NotAvailable(override val line: LineSummary, override val route: Route) : BusArrival(line, route)
    sealed class Available(
        override val line: LineSummary,
        override val route: Route,
        open val bus: BusId,
        open val distance: Int,
        open val isLastBus: Boolean
    ) : BusArrival(line, route) {
        data class Arriving(
            override val line: LineSummary,
            override val route: Route,
            override val bus: BusId,
            override val distance: Int,
            override val isLastBus: Boolean
        ) : Available(line, route, bus, distance, isLastBus)

        data class Estimation(
            override val line: LineSummary,
            override val route: Route,
            override val bus: BusId,
            override val distance: Int,
            override val isLastBus: Boolean,
            val seconds: Int,
        ) : Available(line, route, bus, distance, isLastBus) {
            val minutes: Int?
                get() = round(seconds / 60f).toInt()
        }

        data class NoEstimation(
            override val line: LineSummary,
            override val route: Route,
            override val bus: BusId,
            override val distance: Int,
            override val isLastBus: Boolean
        ) : Available(line, route, bus, distance, isLastBus)
    }
}

/** Limits the list of arrivals to include only one arrival per each bus line. */
fun List<BusArrival>.onePerLine(): List<BusArrival> = this.distinctBy { it.line }
