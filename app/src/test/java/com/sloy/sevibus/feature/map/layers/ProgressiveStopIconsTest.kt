package com.sloy.sevibus.feature.map.layers

import com.sloy.sevibus.domain.model.Position
import com.sloy.sevibus.domain.model.PositionBounds
import com.sloy.sevibus.domain.model.Stop
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.map

class ProgressiveStopIconsTest {

    private val cameraTarget = Position(37.39, -5.99)
    private val visibleArea = PositionBounds(
        northeast = Position(37.40, -5.98),
        southwest = Position(37.38, -6.00),
    )

    @Test
    fun `stops inside the visible area come first, closest to the camera target first`() {
        val stops = listOf(
            stop(1, Position(37.42, -5.99)),
            stop(2, Position(37.399, -5.99)),
            stop(3, Position(37.391, -5.99)),
            stop(4, Position(37.401, -5.99)),
        )

        val sorted = stops.sortedByCameraPriority(visibleArea, cameraTarget)

        expectThat(sorted).map { it.code }.containsExactly(3, 2, 4, 1)
    }

    @Test
    fun `without a visible area stops are sorted by distance to the camera target`() {
        val stops = listOf(
            stop(1, Position(37.42, -5.99)),
            stop(2, Position(37.39, -5.95)),
            stop(3, Position(37.391, -5.99)),
        )

        val sorted = stops.sortedByCameraPriority(visibleArea = null, cameraTarget)

        expectThat(sorted).map { it.code }.containsExactly(3, 1, 2)
    }

    private fun stop(code: Int, position: Position) = Stop(code, "Stop $code", position, emptyList())
}
