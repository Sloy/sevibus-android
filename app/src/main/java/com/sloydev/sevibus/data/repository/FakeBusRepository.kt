package com.sloydev.sevibus.data.repository

import com.sloydev.sevibus.data.database.TussamDao
import com.sloydev.sevibus.data.database.fromEntity
import com.sloydev.sevibus.data.database.summaryFromEntity
import com.sloydev.sevibus.domain.model.Bus
import com.sloydev.sevibus.domain.model.BusArrival
import com.sloydev.sevibus.domain.model.RouteId
import com.sloydev.sevibus.domain.model.StopId
import com.sloydev.sevibus.domain.repository.BusRepository
import kotlinx.coroutines.delay
import kotlin.random.Random

class FakeBusRepository(private val dao: TussamDao) : BusRepository {

    override suspend fun obtainBusArrivals(stop: StopId): List<BusArrival> {
        delay(1000)
        val lines = dao.getLines()
        val routes = dao.getRoutes()
        return dao.getStop(stop).lines.twice().map { line ->
            BusArrival(
                bus = Random.nextInt(),
                distance = Random.nextInt(0, 1000),
                seconds = Random.nextInt(0, 900),
                line = lines.first { it.id == line }.summaryFromEntity(),
                route = routes.first { (line == it.line) and (stop in it.stops) }.fromEntity()
            )
        }.sorted()
    }

    override suspend fun obtainBuses(route: RouteId): List<Bus> {
        return emptyList()
    }
}

private fun <T> List<T>.twice(): List<T> = this + this
