package com.sloy.sevibus.feature.map

import com.sloy.sevibus.domain.model.Bus
import com.sloy.sevibus.domain.model.Line
import com.sloy.sevibus.domain.model.Path
import com.sloy.sevibus.domain.model.Route
import com.sloy.sevibus.domain.model.Stop

sealed interface MapScreenState {
    data object Initial : MapScreenState
    data class Idle(val allStops: List<Stop>) : MapScreenState
    data class LineSelected(val allStops: List<Stop>, val line: Line, val selectedRoute: Route, val lineStops: List<Stop>?, val path: Path?, val buses: List<Bus>?) : MapScreenState, Dismissable, Tickable
    data class LineStopSelected(
        val selectedStop: Stop,
        val lineSelectedState: LineSelected,
    ) : MapScreenState, Dismissable, Tickable

    data class StopSelected(val allStops: List<Stop>, val selectedStop: Stop) : MapScreenState, Dismissable
}

sealed interface Dismissable
sealed interface Tickable

val MapScreenState.selectedLine: Line?
    get() = when (this) {
        is MapScreenState.LineSelected -> this.line
        is MapScreenState.LineStopSelected -> this.lineSelectedState.line
        else -> null
    }


val MapScreenState.allStops: List<Stop>
    get() = when(this){
        is MapScreenState.Initial -> error("Map is still in Initial state")
        is MapScreenState.Idle -> allStops
        is MapScreenState.LineSelected -> allStops
        is MapScreenState.LineStopSelected -> lineSelectedState.allStops
        is MapScreenState.StopSelected -> allStops
    }
