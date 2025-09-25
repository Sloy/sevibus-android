package com.sloy.sevibus.feature.map.states

import com.sloy.sevibus.domain.model.LineId
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.domain.model.moveToPath
import com.sloy.sevibus.domain.repository.RouteRepository
import com.sloy.sevibus.domain.repository.StopRepository
import com.sloy.sevibus.feature.map.MapScreenState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class OnStopAndLineSelected(
    private val stopRepository: StopRepository,
    private val routeRepository: RouteRepository,
    private val onLineSelectedState: OnLineSelectedState,
) {
    suspend operator fun invoke(stopId: StopId, lineId: LineId): Flow<MapScreenState.StopAndLineSelected> = flow {
        val stop = stopRepository.obtainStop(stopId)
        val route = routeRepository.obtainRoute(stopId, lineId)

        emitAll(onLineSelectedState(lineId, route.id)
            .map { lineState ->
                val movedStop = lineState.path?.let { stop.moveToPath(it) } ?: stop
                MapScreenState.StopAndLineSelected(movedStop, lineState)
            }
        )
    }
}
