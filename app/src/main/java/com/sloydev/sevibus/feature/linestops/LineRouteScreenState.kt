package com.sloydev.sevibus.feature.linestops

import com.sloydev.sevibus.domain.model.Line
import com.sloydev.sevibus.domain.model.Route
import com.sloydev.sevibus.domain.model.RouteWithStops
import com.sloydev.sevibus.domain.model.Stop

sealed interface LineRouteScreenState {
    data object Loading : LineRouteScreenState
    data object Error : LineRouteScreenState
    abstract class Content(
        open val line: Line,
    ) : LineRouteScreenState {
        data class Partial(
            override val line: Line,
        ) : Content(line)

        data class Full(
            override val line: Line,
            val routes: List<RouteWithStops>,
        ) : Content(line)
    }

}