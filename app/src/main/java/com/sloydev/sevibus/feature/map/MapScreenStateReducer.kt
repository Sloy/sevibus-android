package com.sloydev.sevibus.feature.map

import com.sloydev.sevibus.domain.model.Bus
import com.sloydev.sevibus.domain.model.Line
import com.sloydev.sevibus.domain.model.Route
import com.sloydev.sevibus.domain.model.RouteId
import com.sloydev.sevibus.domain.model.Stop
import com.sloydev.sevibus.domain.repository.BusRepository
import com.sloydev.sevibus.domain.repository.PathRepository
import com.sloydev.sevibus.domain.repository.StopRepository
import com.sloydev.sevibus.infrastructure.SevLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

class MapScreenStateReducer(
    private val stopRepository: StopRepository,
    private val pathRepository: PathRepository,
    private val busRepository: BusRepository,
) {

    suspend operator fun invoke(state: MapScreenState, action: MapScreenAction): Flow<MapScreenState> = flow {
        when (action) {
            is MapScreenAction.Init -> initIdleState()
            is MapScreenAction.SelectStop -> selectStop(state, action.stop)
            is MapScreenAction.SelectLine -> selectLine(state, action.line)
            is MapScreenAction.Dismiss -> dismiss(state)
            is MapScreenAction.SelectRoute -> selectRoute(state, action.route)
            is MapScreenAction.PeriodicTick -> refresh(state)
        }
    }.onEach { out ->
        SevLogger.logD("${state::class.simpleName} + ${action::class.simpleName} -> ${out::class.simpleName}")
    }.catch { SevLogger.logE(it, "Error on MapScreenStateReducer: ${state::class.simpleName} + ${action::class.simpleName}") }

    private suspend fun FlowCollector<MapScreenState>.initIdleState() {
        val stops = stopRepository.obtainStops()
        emit(MapScreenState.Idle(stops))
    }

    private suspend fun FlowCollector<MapScreenState>.selectRoute(state: MapScreenState, route: Route) {
        if (state is MapScreenState.LineSelected) {
            selectLine(state, state.line, route)
        }
    }

    private suspend fun FlowCollector<MapScreenState>.selectStop(state: MapScreenState, stop: Stop) {
        when (state) {
            is MapScreenState.Initial -> state
            is MapScreenState.Idle -> MapScreenState.StopSelected(state.allStops, selectedStop = stop)
            is MapScreenState.LineSelected -> MapScreenState.LineStopSelected(stop, state)
            is MapScreenState.LineStopSelected -> state.copy(selectedStop = stop)
            is MapScreenState.StopSelected -> state.copy(selectedStop = stop)
        }.let { emit(it) }
    }

    private suspend fun FlowCollector<MapScreenState>.dismiss(state: MapScreenState) {
        return when (state) {
            is MapScreenState.LineStopSelected -> state.lineSelectedState
            is MapScreenState.StopSelected -> MapScreenState.Idle(state.allStops)
            is MapScreenState.LineSelected -> MapScreenState.Idle(stopRepository.obtainStops())
            is MapScreenState.Idle -> state
            is MapScreenState.Initial -> state
        }.let { emit(it) }
    }

    private suspend fun FlowCollector<MapScreenState>.selectLine(state: MapScreenState, line: Line, route: Route = line.routes.first()) {

        val newState = MapScreenState.LineSelected(state.allStops, line, route, null, null, null)
        emit(newState)
        val stops = stopRepository.obtainStops(route.stops)
        emit(newState.copy(lineStops = stops))
        val path = pathRepository.obtainPath(route.id)
        emit(newState.copy(lineStops = stops, path = path))
        refresh(newState.copy(lineStops = stops, path = path))
    }

    private suspend fun FlowCollector<MapScreenState>.refresh(state: MapScreenState) {
        if (state !is Tickable) return
        when (state) {
            is MapScreenState.LineSelected -> state.copy(buses = refreshBuses(state.selectedRoute.id))
            is MapScreenState.LineStopSelected -> state.copy(lineSelectedState = state.lineSelectedState.copy(buses = refreshBuses(state.lineSelectedState.selectedRoute.id)))
        }.let { emit(it) }
    }

    private suspend fun refreshBuses(routeId: RouteId): List<Bus> {
        return busRepository.obtainBuses(routeId)
    }


}

sealed interface MapScreenAction {
    data object Init : MapScreenAction
    data class SelectStop(val stop: Stop) : MapScreenAction
    data class SelectLine(val line: Line) : MapScreenAction
    data class SelectRoute(val route: Route) : MapScreenAction
    data object Dismiss : MapScreenAction
    data object PeriodicTick : MapScreenAction
}

