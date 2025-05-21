package com.sloy.sevibus.domain.model

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly

class BusArrivalTest {


    @Test
    fun `verify sorting order of BusArrival instances`() {
        val arrivals = listOf(
            estimation(bus = 3, seconds = 10 * 60),
            arriving(bus = 1),
            noEstimation(bus = 4),
            notAvailable(),
            estimation(bus = 2, seconds = 5 * 60)
        )

        val sortedArrivals = arrivals.sorted()

        expectThat(sortedArrivals).containsExactly(
            arriving(bus = 1),
            estimation(bus = 2, seconds = 5 * 60),
            estimation(bus = 3, seconds = 10 * 60),
            noEstimation(bus = 4),
            notAvailable()
        )
    }

    private fun arriving(bus: BusId) = BusArrival.Available.Arriving(
        bus = bus,
        distance = 50,
        line = LINE,
        route = ROUTE,
        isLastBus = false
    )

    private fun estimation(bus: BusId, seconds: Int) = BusArrival.Available.Estimation(
        bus = bus,
        distance = 100,
        seconds = seconds,
        line = LINE,
        route = ROUTE,
        isLastBus = false
    )

    private fun noEstimation(bus: BusId) = BusArrival.Available.NoEstimation(
        bus = bus,
        distance = 200,
        line = LINE,
        route = ROUTE,
        isLastBus = false
    )

    private fun notAvailable() = BusArrival.NotAvailable(
        line = LINE,
        route = ROUTE
    )


}
private val LINE = LineSummary(1, "Line 1", LineColor.Red)
private val ROUTE = Route("1.1", 1, "", 1, listOf())
