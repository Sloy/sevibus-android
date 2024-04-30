package com.sloydev.sevibus.feature.map

import com.sloydev.sevibus.domain.model.Line
import com.sloydev.sevibus.domain.model.Path
import com.sloydev.sevibus.domain.model.Route
import com.sloydev.sevibus.domain.model.Stop

sealed interface MapScreenState {
    data object Initial : MapScreenState
    data class Idle(val allStops: List<Stop>) : MapScreenState
    data class LineSelected(val line: Line, val selectedRoute: Route, val lineStops: List<Stop>, val path: Path) : MapScreenState
    data class LineStopSelected(
        val selectedStop: Stop,
        val lineSelectedState: LineSelected,
    ) : MapScreenState

    data class StopSelected(val allStops: List<Stop>, val selectedStop: Stop) : MapScreenState
}

sealed interface MapScreenAction {
    data object Init : MapScreenAction
    data class SelectStop(val stop: Stop) : MapScreenAction
    data object UnselectStop : MapScreenAction
}

