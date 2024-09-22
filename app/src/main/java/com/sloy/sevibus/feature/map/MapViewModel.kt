package com.sloy.sevibus.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloy.sevibus.domain.model.Route
import com.sloy.sevibus.domain.repository.BusRepository
import com.sloy.sevibus.domain.repository.LineRepository
import com.sloy.sevibus.domain.repository.PathRepository
import com.sloy.sevibus.domain.repository.StopRepository
import com.sloy.sevibus.infrastructure.ticker
import com.sloy.sevibus.navigation.NavigationDestination
import com.sloy.sevibus.navigation.TopLevelDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MapViewModel(
    private val lineRepository: LineRepository,
    private val stopRepository: StopRepository,
    private val pathRepository: PathRepository,
    private val busRepository: BusRepository,
) : ViewModel() {

    val state = MutableStateFlow<MapScreenState>(MapScreenState.Initial)
    private val reducer = MapScreenStateReducer(stopRepository, pathRepository, busRepository)

    val ticker = ticker(10_000)
        .filter { state.value is Tickable }
        .onEach { dispatch(MapScreenAction.PeriodicTick) }

    init {
        dispatch(MapScreenAction.Init)
    }

    fun setDestination(destination: NavigationDestination) {
        viewModelScope.launch {
            when (destination) {
                is NavigationDestination.LineStops -> {
                    val line = lineRepository.obtainLine(destination.lineId)
                    dispatch(MapScreenAction.SelectLine(line))
                }

                is NavigationDestination.StopDetail -> {
                    val stop = stopRepository.obtainStop(destination.stopId)
                    dispatch(MapScreenAction.SelectStop(stop))
                }

                //TODO dismiss is probably not the best action, because it has different result
                // depending on the current state. But it will be OK for now
                is TopLevelDestination -> dispatch(MapScreenAction.Dismiss)
            }
        }
    }

    fun onRouteSelected(route: Route) {
        dispatch(MapScreenAction.SelectRoute(route))
    }

    private fun dispatch(action: MapScreenAction) {
        viewModelScope.launch {
            reducer(state.value, action).collect { newState ->
                state.value = newState
            }
        }
    }
}
