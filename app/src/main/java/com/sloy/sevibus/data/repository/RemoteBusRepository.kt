package com.sloy.sevibus.data.repository

import com.sloy.sevibus.data.api.SevibusApi
import com.sloy.sevibus.data.api.model.BusArrivalDto
import com.sloy.sevibus.data.api.model.BusDto
import com.sloy.sevibus.data.database.TussamDao
import com.sloy.sevibus.data.database.fromEntity
import com.sloy.sevibus.data.database.summaryFromEntity
import com.sloy.sevibus.domain.model.Bus
import com.sloy.sevibus.domain.model.BusArrival
import com.sloy.sevibus.domain.model.LineSummary
import com.sloy.sevibus.domain.model.Route
import com.sloy.sevibus.domain.model.RouteId
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.domain.repository.BusRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoteBusRepository(private val api: SevibusApi, private val dao: TussamDao) : BusRepository {
    override suspend fun obtainBusArrivals(stop: StopId): List<BusArrival> = withContext(Dispatchers.Default) {
        val lines = dao.getLines()
        val routes = dao.getRoutes()
        return@withContext api.getArrivals(stop).map { arrival ->
            val line = lines.first { it.id == arrival.line }.summaryFromEntity()
            val route = routes.first { (arrival.line == it.line) and (stop in it.stops) }.fromEntity()
            arrival.fromDto(line, route)
        }.sorted()
    }

    override suspend fun obtainBuses(route: RouteId): List<Bus> = withContext(Dispatchers.Default) {
        api.getBuses(route).map { it.fromDto() }
    }
}

private fun BusDto.fromDto(): Bus {
    return Bus(id, position.fromDto(), positionInLine, direction)
}

private fun BusArrivalDto.fromDto(line: LineSummary, route: Route): BusArrival {
    return BusArrival(bus, distance, seconds, line, route)
}
