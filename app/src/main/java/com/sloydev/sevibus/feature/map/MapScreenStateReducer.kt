package com.sloydev.sevibus.feature.map

import com.sloydev.sevibus.domain.model.Line
import com.sloydev.sevibus.domain.model.Stop
import com.sloydev.sevibus.domain.repository.PathRepository
import com.sloydev.sevibus.domain.repository.StopRepository
import com.sloydev.sevibus.infrastructure.SevLogger
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class MapScreenStateReducer(
    private val stopRepository: StopRepository,
    private val pathRepository: PathRepository,
) {

    suspend operator fun invoke(state: MapScreenState, action: MapScreenAction): MapScreenState {
        return when (action) {
            is MapScreenAction.Init -> initIdleState()
            is MapScreenAction.SelectStop -> selectStop(state, action.stop)
            is MapScreenAction.UnselectStop -> unselectStop(state)
            is MapScreenAction.SelectLine -> selectLine(state, action.line)
            is MapScreenAction.UnselectLine -> TODO()
        }.apply {
            SevLogger.logD("${state::class.simpleName} + ${action::class.simpleName} -> ${this::class.simpleName}")
        }

    }

    private suspend fun initIdleState(): MapScreenState.Idle {
        val stops = stopRepository.obtainStops()
        return MapScreenState.Idle(stops)
    }

    private fun selectStop(state: MapScreenState, stop: Stop): MapScreenState {
        return when (state) {
            is MapScreenState.Initial -> state
            is MapScreenState.Idle -> MapScreenState.StopSelected(state.allStops, selectedStop = stop)
            is MapScreenState.LineSelected -> MapScreenState.LineStopSelected(stop, state)
            is MapScreenState.LineStopSelected -> state.copy(selectedStop = stop)
            is MapScreenState.StopSelected -> state.copy(selectedStop = stop)
        }
    }

    private fun unselectStop(state: MapScreenState): MapScreenState {
        return when (state) {
            is MapScreenState.LineStopSelected -> state.lineSelectedState
            is MapScreenState.StopSelected -> MapScreenState.Idle(state.allStops)
            else -> state
        }
    }

    private suspend fun selectLine(state: MapScreenState, line: Line): MapScreenState {
        return coroutineScope {
            val defaultRoute = line.routes.first()
            val stops = async { stopRepository.obtainStops(defaultRoute.stops) }
            val path = async { pathRepository.obtainPath(defaultRoute.id) }
            MapScreenState.LineSelected(
                line, defaultRoute, lineStops = stops.await(), path = path.await()
            )
        }
    }

}