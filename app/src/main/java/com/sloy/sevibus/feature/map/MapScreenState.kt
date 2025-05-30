package com.sloy.sevibus.feature.map

import com.sloy.sevibus.domain.model.Bus
import com.sloy.sevibus.domain.model.Line
import com.sloy.sevibus.domain.model.Path
import com.sloy.sevibus.domain.model.Stop

sealed interface MapScreenState {
    data object Initial : MapScreenState
    data class Idle(val allStops: List<Stop>) : MapScreenState
    data class LinesOverview(val allStops: List<Stop>, val linePaths: List<Path>? = null) : MapScreenState
    data class LineSelected(
        val otherStops: List<Stop>,
        val line: Line,
        val lineStops: List<Stop>,
        val path: Path?,
        val buses: List<Bus>?
    ) : MapScreenState

    data class StopAndLineSelected(
        val selectedStop: Stop,
        val lineSelectedState: LineSelected,
    ) : MapScreenState

    data class StopSelected(
        val otherStops: List<Stop>,
        val selectedStop: Stop,
        val linesPaths: List<Path>? = null,
        val buses: List<Bus>? = null,
    ) : MapScreenState
}
