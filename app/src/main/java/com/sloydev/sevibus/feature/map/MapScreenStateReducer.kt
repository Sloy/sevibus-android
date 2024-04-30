package com.sloydev.sevibus.feature.map

import com.sloydev.sevibus.domain.model.Stop
import com.sloydev.sevibus.domain.repository.StopRepository
import com.sloydev.sevibus.infrastructure.SevLogger

class MapScreenStateReducer(private val stopRepository: StopRepository) {

    suspend operator fun invoke(state: MapScreenState, action: MapScreenAction): MapScreenState {
        return when (action) {
            is MapScreenAction.Init -> initIdleState()
            is MapScreenAction.SelectStop -> selectStop(state, action.stop)
            is MapScreenAction.UnselectStop -> unselectStop(state)
            else -> TODO("Action reducer not implemented: $action")
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
            is MapScreenState.Initial -> error("selectStop from Initial state not allowed")
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

}