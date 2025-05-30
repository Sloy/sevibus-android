package com.sloy.sevibus.feature.linestops

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloy.sevibus.domain.model.LineId
import com.sloy.sevibus.domain.model.Route
import com.sloy.sevibus.domain.model.RouteId
import com.sloy.sevibus.domain.model.Stop
import com.sloy.sevibus.domain.repository.LineRepository
import com.sloy.sevibus.domain.repository.StopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LineRouteViewModel(
    private val lineId: LineId,
    private val initialRouteId: RouteId?,
    private val lineRepository: LineRepository,
    private val stopRepository: StopRepository,
) : ViewModel() {

    val state = MutableStateFlow<LineRouteScreenState>(LineRouteScreenState.Loading)
    private lateinit var selectedRoute: Route
    private lateinit var stopsFromRoute: Map<RouteId, List<Stop>>

    init {
        viewModelScope.launch {
            runCatching {
                val line = lineRepository.obtainLine(lineId)
                selectedRoute = line.routes.find { it.id == initialRouteId } ?: line.routes.first()
                state.value = LineRouteScreenState.Content.Partial(line, selectedRoute)

                stopsFromRoute = line.routes.associate { it.id to stopRepository.obtainStops(it.stops) }
                state.value = LineRouteScreenState.Content.Full(line, selectedRoute, stopsFromRoute[selectedRoute.id]!!)
            }.onFailure {
                state.value = LineRouteScreenState.Error
            }
        }
    }

    fun onRouteSelected(route: Route) {
        selectedRoute = route
        state.update { state ->
            check(state is LineRouteScreenState.Content.Full)
            state.copy(selectedRoute = selectedRoute, stops = stopsFromRoute[selectedRoute.id]!!)
        }
    }

}
