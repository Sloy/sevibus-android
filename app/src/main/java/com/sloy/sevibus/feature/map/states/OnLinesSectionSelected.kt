package com.sloy.sevibus.feature.map.states

import com.sloy.sevibus.domain.model.LineColor
import com.sloy.sevibus.domain.model.nudgeByColors
import com.sloy.sevibus.domain.repository.LineRepository
import com.sloy.sevibus.domain.repository.PathRepository
import com.sloy.sevibus.domain.repository.RouteRepository
import com.sloy.sevibus.domain.repository.StopRepository
import com.sloy.sevibus.feature.map.MapScreenState
import com.sloy.sevibus.infrastructure.FeatureFlags
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class OnLinesSectionSelected(
    private val stopRepository: StopRepository,
    private val lineRepository: LineRepository,
    private val pathRepository: PathRepository,
    private val routeRepository: RouteRepository,
) {

    suspend operator fun invoke(): Flow<MapScreenState.LinesOverview> = flow {
        val stops = stopRepository.obtainStops()
        emit(MapScreenState.LinesOverview(stops))

        if (FeatureFlags.showLinesOverview) {
            val routes = routeRepository.obtainRoutes()
            val paths = pathRepository.obtainPaths(routes.map { it.id })
                .filter { it.line.color != LineColor.Black }
                .filter { it.line.color != LineColor.Wine }

            emit(MapScreenState.LinesOverview(stops, paths.nudgeByColors()))
        }
    }
}
