package com.sloy.sevibus.feature.map

import com.sloy.sevibus.domain.model.Position
import com.sloy.sevibus.domain.model.PositionBounds
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log2
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

data class EdgeInsets(val left: Float = 0f, val top: Float = 0f, val right: Float = 0f, val bottom: Float = 0f)

/**
 * The map view in pixels. [cameraPadding] is the padding given to the map, which places the camera target in the middle
 * of the padded area. [fitArea] is the area, measured from the map edges, where bounds have to fit.
 */
data class MapViewport(
    val width: Float,
    val height: Float,
    val cameraPadding: EdgeInsets,
    val fitArea: EdgeInsets,
    val density: Float,
)

/**
 * Fits bounds in an area of the map that doesn't have to match the map padding.
 *
 * Google Maps' newLatLngBounds fits in the padded area, and when the zoom it needs is below the minimum zoom preference
 * it centers the bounds in the whole map instead of the padded area. Computing the camera here avoids both.
 */
object CameraFit {

    data class Camera(val target: Position, val zoom: Float)

    fun fit(bounds: PositionBounds, viewport: MapViewport, minZoom: Float, maxZoom: Float): Camera {
        val west = mercatorX(bounds.southwest.longitude)
        val east = mercatorX(bounds.northeast.longitude)
        val north = mercatorY(bounds.northeast.latitude)
        val south = mercatorY(bounds.southwest.latitude)

        val fit = viewport.fitArea
        val fitWidth = viewport.width - fit.left - fit.right
        val fitHeight = viewport.height - fit.top - fit.bottom
        val tileSize = TILE_SIZE_DP * viewport.density

        val zoomForWidth = zoomToFit(fitWidth, (east - west) * tileSize)
        val zoomForHeight = zoomToFit(fitHeight, (south - north) * tileSize)
        val zoom = min(zoomForWidth, zoomForHeight).coerceIn(minZoom.toDouble(), maxZoom.toDouble())
        val worldSize = tileSize * 2.0.pow(zoom)

        val padding = viewport.cameraPadding
        val paddedCenterX = padding.left + (viewport.width - padding.left - padding.right) / 2
        val paddedCenterY = padding.top + (viewport.height - padding.top - padding.bottom) / 2
        val fitCenterX = fit.left + fitWidth / 2
        val fitCenterY = fit.top + fitHeight / 2

        val targetX = (west + east) / 2 + (paddedCenterX - fitCenterX) / worldSize
        val targetY = (north + south) / 2 + (paddedCenterY - fitCenterY) / worldSize
        return Camera(Position(latitude(targetY), longitude(targetX)), zoom.toFloat())
    }

    private fun zoomToFit(availablePx: Float, sizeAtZoomZeroPx: Double): Double {
        if (sizeAtZoomZeroPx <= 0.0) return Double.MAX_VALUE
        return log2(availablePx / sizeAtZoomZeroPx)
    }
}

private const val TILE_SIZE_DP = 256

internal fun mercatorX(longitude: Double): Double = (longitude + 180) / 360

internal fun mercatorY(latitude: Double): Double {
    val sinLatitude = sin(Math.toRadians(latitude))
    return 0.5 - ln((1 + sinLatitude) / (1 - sinLatitude)) / (4 * PI)
}

private fun longitude(x: Double): Double = x * 360 - 180

private fun latitude(y: Double): Double = 90 - 360 * atan(exp((y - 0.5) * 2 * PI)) / PI
