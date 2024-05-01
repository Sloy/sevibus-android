package com.sloydev.sevibus.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloydev.sevibus.domain.model.Line
import com.sloydev.sevibus.domain.model.Route
import com.sloydev.sevibus.domain.model.Stop
import com.sloydev.sevibus.domain.repository.LineRepository
import com.sloydev.sevibus.domain.repository.PathRepository
import com.sloydev.sevibus.domain.repository.StopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel(
    private val lineRepository: LineRepository,
    private val stopsRepository: StopRepository,
    private val pathRepository: PathRepository,
) : ViewModel() {

    val state = MutableStateFlow<MapScreenState>(MapScreenState.Initial)
    private val reducer = MapScreenStateReducer(stopsRepository, pathRepository)

    init {
        dispatch(MapScreenAction.Init)
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

    fun onDismiss(){
        dispatch(MapScreenAction.Dismiss)
    }

    private fun dispatch(action: MapScreenAction) {
        viewModelScope.launch {
            state.update { reducer(it, action) }
        }
    }
}
