package com.sloy.sevibus.feature.map.layers

import androidx.compose.runtime.MonotonicFrameClock
import com.google.android.gms.maps.model.BitmapDescriptor
import com.sloy.sevibus.domain.model.Position
import com.sloy.sevibus.domain.model.PositionBounds
import com.sloy.sevibus.domain.model.Stop
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
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

    @Test
    fun `first load sets the icons in batches of markers per frame`() = runTest(frameClock) {
        val stopIcons = ProgressiveStopIcons(markersPerFrame = 2)

        stopIcons.update(fiveStops, colorIcon)

        expectThat(frameClock.frames).isEqualTo(2)
        expectThat(fiveStops.map { stopIcons.iconOf(it).value }).all { isEqualTo(colorIcon) }
    }

    @Test
    fun `after the first load icons are swapped in a single frame`() = runTest(frameClock) {
        val stopIcons = ProgressiveStopIcons(markersPerFrame = 2)
        stopIcons.update(fiveStops, colorIcon)
        frameClock.frames = 0

        stopIcons.update(fiveStops, grayIcon)

        expectThat(frameClock.frames).isEqualTo(0)
        expectThat(fiveStops.map { stopIcons.iconOf(it).value }).all { isEqualTo(grayIcon) }
    }

    @Test
    fun `after the first load icons are hidden in a single frame`() = runTest(frameClock) {
        val stopIcons = ProgressiveStopIcons(markersPerFrame = 2)
        stopIcons.update(fiveStops, colorIcon)
        frameClock.frames = 0

        stopIcons.update(fiveStops, icon = null)

        expectThat(frameClock.frames).isEqualTo(0)
        expectThat(fiveStops.map { stopIcons.iconOf(it).value }).all { isNull() }
    }

    @Test
    fun `an empty update doesn't count as the first load`() = runTest(frameClock) {
        val stopIcons = ProgressiveStopIcons(markersPerFrame = 2)
        stopIcons.update(emptyList(), colorIcon)

        stopIcons.update(fiveStops, colorIcon)

        expectThat(frameClock.frames).isEqualTo(2)
    }

    private val colorIcon = mock<BitmapDescriptor>()
    private val grayIcon = mock<BitmapDescriptor>()
    private val fiveStops = (1..5).map { stop(it, cameraTarget) }
    private val frameClock = CountingFrameClock()

    private class CountingFrameClock : MonotonicFrameClock {
        var frames = 0

        override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
            frames++
            return onFrame(frames.toLong())
        }
    }

    private fun stop(code: Int, position: Position) = Stop(code, "Stop $code", position, emptyList())
}
