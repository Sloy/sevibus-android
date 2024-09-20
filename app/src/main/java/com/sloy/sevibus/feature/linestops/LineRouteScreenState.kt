package com.sloy.sevibus.feature.linestops

import com.sloy.sevibus.domain.model.Line
import com.sloy.sevibus.domain.model.Route
import com.sloy.sevibus.domain.model.Stop

sealed interface LineRouteScreenState {
    data object Loading : LineRouteScreenState
    data object Error : LineRouteScreenState
    abstract class Content(
        open val line: Line,
        open val selectedRoute: Route,
    ) : LineRouteScreenState {
        data class Partial(
            override val line: Line,
            override val selectedRoute: Route,
        ) : Content(line, selectedRoute)

        data class Full(
            override val line: Line,
            override val selectedRoute: Route,
            val stops: List<Stop>,
        ) : Content(line, selectedRoute)
    }

}