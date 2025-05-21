package com.sloy.sevibus.data.repository

import com.sloy.sevibus.data.database.TussamDao
import com.sloy.sevibus.data.database.fromEntity
import com.sloy.sevibus.data.database.summaryFromEntity
import com.sloy.sevibus.domain.model.Bus
import com.sloy.sevibus.domain.model.BusArrival
import com.sloy.sevibus.domain.model.RouteId
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.domain.repository.BusRepository
import kotlinx.coroutines.delay
import kotlin.random.Random

class FakeBusRepository(private val dao: TussamDao) : BusRepository {

    override suspend fun obtainBusArrivals(stop: StopId): List<BusArrival> {
        delay(1000)
        val lines = dao.getLines()
        val routes = dao.getRoutes()
        return dao.getStop(stop).lines.twice().map { line ->
            BusArrival.Available.Estimation(
                bus = Random.nextInt(),
                distance = Random.nextInt(0, 1000),
                seconds = Random.nextInt(0, 900),
                line = lines.first { it.id == line }.summaryFromEntity(),
                route = routes.first { (line == it.line) and (stop in it.stops) }.fromEntity(),
                isLastBus = false,

            )
        }.sorted()
    }

    override suspend fun obtainBuses(route: RouteId): List<Bus> {
        return emptyList()
    }
}

private fun <T> List<T>.twice(): List<T> = this + this
