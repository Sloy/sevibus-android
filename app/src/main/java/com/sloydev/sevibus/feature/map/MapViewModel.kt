package com.sloydev.sevibus.feature.map

import androidx.lifecycle.ViewModel
import com.sloydev.sevibus.domain.model.Line
import com.sloydev.sevibus.domain.model.Route
import com.sloydev.sevibus.domain.model.RouteId
import com.sloydev.sevibus.domain.model.RoutePath
import com.sloydev.sevibus.domain.model.Stop
import com.sloydev.sevibus.domain.repository.LineRepository
import com.sloydev.sevibus.domain.repository.StopRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

class MapViewModel(
    private val lineRepository: LineRepository,
    private val stopsRepository: StopRepository
) : ViewModel() {

    val selectedLine = MutableStateFlow<Line?>(null)
    val selectedRoute = MutableStateFlow<Route?>(null)

    /*private val selectedRoute: Flow<Route> = selectedLine.filterNotNull()
        .combine(selectedRouteId.filterNotNull()) { line, routeId ->
            line.routes.first { it.id == routeId }
        }
    */

    val stops: Flow<List<Stop>> = selectedRoute.filterNotNull().map { route ->
        stopsRepository.obtainStops(route.stops)
    }


    fun onLineSelected(line: Line?) {
        selectedLine.value = line
        selectedRoute.value = line?.routes?.first()
    }

    fun onRouteSelected(route: Route) {
        selectedRoute.value = route
    }
}

sealed interface MapScreenState {
    data object Unselected : MapScreenState
    data class SelectedLine(
        val line: Line,
        val selectedRoute: RouteId,
        val path: RoutePath,
        val stops: List<Stop>
    ) : MapScreenState
}
