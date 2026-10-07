package com.sloy.sevibus.feature.map

import com.sloy.sevibus.domain.model.Position
import com.sloy.sevibus.domain.model.PositionBounds
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isLessThan
import kotlin.math.abs

class CameraFitTest {

    private val density = 2f
    private val viewport = MapViewport(
        width = 800f,
        height = 1600f,
        cameraPadding = EdgeInsets(top = 160f, bottom = 900f),
        fitArea = EdgeInsets(left = 20f, top = 80f, right = 20f, bottom = 920f),
        density = density,
    )
    private val bounds = PositionBounds(
        northeast = Position(37.41, -5.96),
        southwest = Position(37.36, -6.01),
    )

    @Test
    fun `fitted bounds touch the fit area on the tightest side`() {
        val camera = CameraFit.fit(bounds, viewport, minZoom = 0f, maxZoom = 21f)

        val northEdge = viewport.screenY(bounds.northeast.latitude, camera)
        val southEdge = viewport.screenY(bounds.southwest.latitude, camera)
        expectThat(abs(northEdge - 80f)).isLessThan(0.5f)
        expectThat(abs(southEdge - (1600f - 920f))).isLessThan(0.5f)
    }

    @Test
    fun `bounds center is drawn at the center of the fit area, not of the camera padding`() {
        val camera = CameraFit.fit(bounds, viewport, minZoom = 0f, maxZoom = 21f)

        val center = mercatorY(bounds.northeast.latitude).plus(mercatorY(bounds.southwest.latitude)) / 2
        val centerOnScreen = viewport.screenYOfMercator(center, camera)
        expectThat(abs(centerOnScreen - (80f + (1600f - 80f - 920f) / 2))).isLessThan(0.5f)
    }

    @Test
    fun `the zoom is clamped to the minimum and the bounds stay centered in the fit area`() {
        val camera = CameraFit.fit(bounds, viewport, minZoom = 14f, maxZoom = 21f)

        expectThat(camera.zoom).isEqualTo(14f)
        val center = mercatorY(bounds.northeast.latitude).plus(mercatorY(bounds.southwest.latitude)) / 2
        val centerOnScreen = viewport.screenYOfMercator(center, camera)
        expectThat(abs(centerOnScreen - (80f + (1600f - 80f - 920f) / 2))).isLessThan(0.5f)
    }

    @Test
    fun `a single point uses the maximum zoom`() {
        val point = PositionBounds(northeast = Position(37.39, -5.99), southwest = Position(37.39, -5.99))

        val camera = CameraFit.fit(point, viewport, minZoom = 11f, maxZoom = 16f)

        expectThat(camera.zoom).isEqualTo(16f)
    }

    @Test
    fun `the target keeps the bounds horizontally centered when the fit area is symmetric`() {
        val camera = CameraFit.fit(bounds, viewport, minZoom = 0f, maxZoom = 21f)

        expectThat(abs(camera.target.longitude - (-5.985))).isLessThan(0.000001)
    }

    private fun MapViewport.screenY(latitude: Double, camera: CameraFit.Camera): Float =
        screenYOfMercator(mercatorY(latitude), camera)

    private fun MapViewport.screenYOfMercator(y: Double, camera: CameraFit.Camera): Float {
        val worldSize = 256 * density * Math.pow(2.0, camera.zoom.toDouble())
        val paddedCenterY = cameraPadding.top + (height - cameraPadding.top - cameraPadding.bottom) / 2
        return (paddedCenterY + (y - mercatorY(camera.target.latitude)) * worldSize).toFloat()
    }
}
