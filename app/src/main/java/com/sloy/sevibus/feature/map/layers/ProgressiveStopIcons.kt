package com.sloy.sevibus.feature.map.layers

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.withFrameNanos
import com.google.android.gms.maps.model.BitmapDescriptor
import com.sloy.sevibus.domain.model.Position
import com.sloy.sevibus.domain.model.PositionBounds
import com.sloy.sevibus.domain.model.Stop
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.domain.model.contains
import com.sloy.sevibus.domain.model.manhattanDistance

@Stable
class ProgressiveStopIcons(private val markersPerFrame: Int) {
    private val icons = HashMap<StopId, MutableState<BitmapDescriptor?>>()
    private var isFirstLoadDone = false

    fun iconOf(stop: Stop): State<BitmapDescriptor?> = stateOf(stop)

    suspend fun update(prioritizedStops: List<Stop>, icon: BitmapDescriptor?) {
        if (isFirstLoadDone) {
            prioritizedStops.forEach { stateOf(it).value = icon }
            return
        }
        var updatedInFrame = 0
        prioritizedStops.forEach { stop ->
            val state = stateOf(stop)
            if (state.value == icon) return@forEach
            state.value = icon
            updatedInFrame++
            if (updatedInFrame == markersPerFrame) {
                withFrameNanos { }
                updatedInFrame = 0
            }
        }
        isFirstLoadDone = prioritizedStops.isNotEmpty() && icon != null
    }

    private fun stateOf(stop: Stop) = icons.getOrPut(stop.code) { mutableStateOf(null) }
}

fun List<Stop>.sortedByCameraPriority(visibleArea: PositionBounds?, cameraTarget: Position): List<Stop> =
    sortedWith(
        compareBy<Stop> { visibleArea?.contains(it.position) != true }
            .thenBy { it.position.manhattanDistance(cameraTarget) }
    )
