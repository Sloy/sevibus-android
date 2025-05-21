package com.sloy.sevibus.feature.map.states

import com.sloy.sevibus.domain.model.LineId
import com.sloy.sevibus.domain.model.RouteId
import com.sloy.sevibus.domain.model.moveStopsToPath
import com.sloy.sevibus.domain.repository.BusRepository
import com.sloy.sevibus.domain.repository.LineRepository
import com.sloy.sevibus.domain.repository.PathRepository
import com.sloy.sevibus.domain.repository.RouteRepository
import com.sloy.sevibus.domain.repository.StopRepository
import com.sloy.sevibus.feature.map.MapScreenState
import com.sloy.sevibus.infrastructure.SevLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.seconds

class OnLineSelectedState(
    private val lineRepository: LineRepository,
    private val stopRepository: StopRepository,
    private val pathRepository: PathRepository,
    private val busRepository: BusRepository,
    private val routeRepository: RouteRepository,
) {

    suspend operator fun invoke(lineId: LineId, routeId: RouteId?): Flow<MapScreenState.LineSelected> = flow {
        val allStops = stopRepository.obtainStops()
        val line = lineRepository.obtainLine(lineId)

        val route = if (routeId != null) {
            routeRepository.obtainRoute(routeId)
        } else {
            routeRepository.obtainRoute(line.routes.first().id)
        }

        val routeStops = stopRepository.obtainStops(route.stops)
        val otherStops = allStops - routeStops

        emit(
            MapScreenState.LineSelected(
                otherStops = otherStops,
                line = line,
                lineStops = routeStops,
                path = null,
                buses = null
            )
        )

        val path = runCatching {
            pathRepository.obtainPath(route.id)
        }.onSuccess {
            emit(
                MapScreenState.LineSelected(
                    otherStops = otherStops,
                    line = line,
                    lineStops = routeStops.moveStopsToPath(it),
                    path = it,
                    buses = null
                )
            )
        }.onFailure { SevLogger.logW(it, "Error obtaining path from route ${route.id}") }
            .getOrNull()

        while (true) {
            runCatching {
                busRepository.obtainBuses(route.id)
            }.onSuccess { buses ->
                emit(
                    MapScreenState.LineSelected(
                        otherStops = otherStops,
                        line = line,
                        lineStops = routeStops.moveStopsToPath(path),
                        path = path,
                        buses = buses
                    )
                )
            }.onFailure { SevLogger.logW(it, "Error obtaining buses from route ${route.id}") }
            delay(10.seconds)
        }
    }
}
