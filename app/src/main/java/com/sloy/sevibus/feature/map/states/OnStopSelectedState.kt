package com.sloy.sevibus.feature.map.states

import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.domain.model.nudge
import com.sloy.sevibus.domain.repository.BusRepository
import com.sloy.sevibus.domain.repository.PathRepository
import com.sloy.sevibus.domain.repository.RouteRepository
import com.sloy.sevibus.domain.repository.StopRepository
import com.sloy.sevibus.feature.map.MapScreenState
import com.sloy.sevibus.infrastructure.SevLogger
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.seconds

class OnStopSelectedState(
    private val stopRepository: StopRepository,
    private val pathRepository: PathRepository,
    private val routeRepository: RouteRepository,
    private val busRepository: BusRepository,
) {

    suspend operator fun invoke(stopId: StopId): Flow<MapScreenState.StopSelected> = flow {
        val stop = stopRepository.obtainStop(stopId)
        val otherStops = stopRepository.obtainStops() - stop
        val stopRoutes = routeRepository.obtainRoutesOfStop(stopId)

        emit(MapScreenState.StopSelected(otherStops = otherStops, selectedStop = stop))
        val paths = pathRepository.obtainPaths(stopRoutes.map { it.id }).nudge()
        emit(MapScreenState.StopSelected(otherStops = otherStops, selectedStop = stop, linesPaths = paths))

        while (true) {
            coroutineScope {
                val buses = stopRoutes.map { route ->
                    async { runCatching { busRepository.obtainBuses(route.id) }
                        .onFailure { SevLogger.logE(it, "Error obtaining buses for stop $stopId") }
                        .getOrElse { emptyList() } }
                }.awaitAll().flatten()
                emit(MapScreenState.StopSelected(otherStops = otherStops, selectedStop = stop, linesPaths = paths, buses = buses))
            }
            delay(10.seconds)
        }

    }
}
