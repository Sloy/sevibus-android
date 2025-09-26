package com.sloy.sevibus.data.repository

import com.sloy.sevibus.data.api.SevibusApi
import com.sloy.sevibus.data.api.model.BusArrivalDto
import com.sloy.sevibus.data.api.model.BusDto
import com.sloy.sevibus.data.cache.ArrivalsMemoryCache
import com.sloy.sevibus.data.cache.BusesMemoryCache
import com.sloy.sevibus.domain.model.Bus
import com.sloy.sevibus.domain.model.BusArrival
import com.sloy.sevibus.domain.model.LineSummary
import com.sloy.sevibus.domain.model.Route
import com.sloy.sevibus.domain.model.RouteId
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.domain.model.lineId
import com.sloy.sevibus.domain.model.toSummary
import com.sloy.sevibus.domain.repository.BusRepository
import com.sloy.sevibus.domain.repository.LineRepository
import com.sloy.sevibus.domain.repository.RouteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class RemoteBusRepository(
    private val lineRepository: LineRepository,
    private val routeRepository: RouteRepository,
    private val api: SevibusApi,
    private val arrivalsMemoryCache: ArrivalsMemoryCache,
    private val busesMemoryCache: BusesMemoryCache,
) : BusRepository {

    private val arrivalsMutexMap = ConcurrentHashMap<StopId, Mutex>()
    private val busesMutexMap = ConcurrentHashMap<RouteId, Mutex>()

    override suspend fun obtainBusArrivals(stop: StopId): List<BusArrival> = withContext(Dispatchers.Default) {
        val mutex = arrivalsMutexMap.computeIfAbsent(stop) { Mutex() }
        mutex.withLock {
            arrivalsMemoryCache.get(stop)?.let { return@withContext it }
            val arrivals = api.getArrivals(stop)
            val routes = routeRepository.obtainRoutesOfStop(stop)
            val lines = lineRepository.obtainLines(routes.map { it.line }).map { it.toSummary() }

            val successfulArrivals = arrivals.map { arrival ->
                val line = lines.first { it.id == arrival.line }
                val route = routes.first { (arrival.line == it.line) and (stop in it.stops) }
                arrival.fromDto(line, route)
            }

            val missingArrivals = getMissingArrivals(lines, routes, successfulArrivals)

            return@withContext (successfulArrivals + missingArrivals).sorted()
                .also { arrivalsMemoryCache.put(stop, it) }
        }
    }

    override suspend fun obtainBuses(route: RouteId): List<Bus> = withContext(Dispatchers.Default) {
        val mutex = busesMutexMap.computeIfAbsent(route) { Mutex() }
        mutex.withLock {
            val line = lineRepository.obtainLine(route.lineId).toSummary()
            busesMemoryCache.get(route)?.let { return@withContext it }
            api.getBuses(route).map { it.fromDto(line) }
                .also { busesMemoryCache.put(route, it) }
        }
    }

    private fun getMissingArrivals(
        allLines: List<LineSummary>,
        allRoutes: List<Route>,
        successfulArrivals: List<BusArrival>
    ): List<BusArrival.NotAvailable> {
        val missingLines = allLines.filter { line -> successfulArrivals.none { it.line == line } }
        val routesAndLines: List<Pair<LineSummary, Route>> = missingLines.map { line ->
            line to (allRoutes.find { it.line == line.id })
        }.filter { (_, route) -> route != null }.map { (line, route) -> line to route!! }
        return routesAndLines.map { (line, route) ->
            BusArrival.NotAvailable(line, route)
        }
    }
}

private fun BusDto.fromDto(line: LineSummary): Bus {
    return Bus(id, position.fromDto(), positionInLine, direction, line)
}

private fun BusArrivalDto.fromDto(line: LineSummary, route: Route): BusArrival {
    return when (seconds) {
        0 -> BusArrival.Available.Arriving(line, route, bus, distance, isLastBus)
        null -> BusArrival.Available.NoEstimation(line, route, bus, distance, isLastBus)
        else -> BusArrival.Available.Estimation(line, route, bus, distance, isLastBus, seconds)
    }
}
