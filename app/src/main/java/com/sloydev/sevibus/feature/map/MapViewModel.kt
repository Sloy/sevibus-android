package com.sloydev.sevibus.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloydev.sevibus.domain.model.Line
import com.sloydev.sevibus.domain.model.Route
import com.sloydev.sevibus.domain.model.Stop
import com.sloydev.sevibus.domain.repository.BusRepository
import com.sloydev.sevibus.domain.repository.LineRepository
import com.sloydev.sevibus.domain.repository.PathRepository
import com.sloydev.sevibus.domain.repository.StopRepository
import com.sloydev.sevibus.infrastructure.ticker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MapViewModel(
    private val lineRepository: LineRepository,
    private val stopsRepository: StopRepository,
    private val pathRepository: PathRepository,
    private val busRepository: BusRepository,
) : ViewModel() {

    val state = MutableStateFlow<MapScreenState>(MapScreenState.Initial)
    private val reducer = MapScreenStateReducer(stopsRepository, pathRepository, busRepository)

    init {
        dispatch(MapScreenAction.Init)
        ticker(10_000)
            .filter { state.value is Tickable } // Emit only when state is State2
            .onEach { dispatch(MapScreenAction.PeriodicTick) }
            .launchIn(viewModelScope)
    }

    fun onStopSelected(stop: Stop) {
        dispatch(MapScreenAction.SelectStop(stop))
    }

    fun onLineSelected(line: Line) {
        dispatch(MapScreenAction.SelectLine(line))
    }

    fun onRouteSelected(route: Route) {
        dispatch(MapScreenAction.SelectRoute(route))
    }

    fun onDismiss() {
        dispatch(MapScreenAction.Dismiss)
    }

    private fun dispatch(action: MapScreenAction) {
        viewModelScope.launch {
            reducer(state.value, action).collect { newState ->
                state.value = newState
            }
        }
    }
}
