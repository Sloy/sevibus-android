package com.sloydev.sevibus.feature.map

import com.sloydev.sevibus.domain.model.Line
import com.sloydev.sevibus.domain.model.Route
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
            is MapScreenAction.SelectLine -> selectLine(state, action.line)
            is MapScreenAction.Dismiss -> dismiss(state)
            is MapScreenAction.SelectRoute -> selectRoute(state, action.route)
        }.apply {
            SevLogger.logD("${state::class.simpleName} + ${action::class.simpleName} -> ${this::class.simpleName}")
        }

    }

    private suspend fun initIdleState(): MapScreenState.Idle {
        val stops = stopRepository.obtainStops()
        return MapScreenState.Idle(stops)
    }

    private suspend fun selectRoute(state: MapScreenState, route: Route): MapScreenState {
        if (state !is MapScreenState.LineSelected) {
            error("Not allowed")
        }
        return selectLine(state, state.line, route)
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

    private suspend fun dismiss(state: MapScreenState): MapScreenState {
        return when (state) {
            is MapScreenState.LineStopSelected -> state.lineSelectedState
            is MapScreenState.StopSelected -> MapScreenState.Idle(state.allStops)
            is MapScreenState.LineSelected -> MapScreenState.Idle(stopRepository.obtainStops())
            is MapScreenState.Idle -> state
            is MapScreenState.Initial -> state
        }
    }

    private suspend fun selectLine(state: MapScreenState, line: Line, route: Route = line.routes.first()): MapScreenState {
        return coroutineScope {
            val stops = async { stopRepository.obtainStops(route.stops) }
            val path = async { pathRepository.obtainPath(route.id) }
            MapScreenState.LineSelected(
                line, route, lineStops = stops.await(), path = path.await()
            )
        }
    }

}

sealed interface MapScreenAction {
    data object Init : MapScreenAction
    data class SelectStop(val stop: Stop) : MapScreenAction
    data class SelectLine(val line: Line) : MapScreenAction
    data class SelectRoute(val route: Route) : MapScreenAction
    data object Dismiss : MapScreenAction
}

