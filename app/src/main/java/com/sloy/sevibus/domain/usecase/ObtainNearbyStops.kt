package com.sloy.sevibus.domain.usecase

import com.sloy.sevibus.domain.model.Position
import com.sloy.sevibus.domain.model.Stop
import com.sloy.sevibus.domain.model.manhattanDistance
import com.sloy.sevibus.domain.repository.StopRepository
import com.sloy.sevibus.feature.foryou.nearby.NearbyStop
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class ObtainNearbyStops(private val stopRepository: StopRepository) {

    suspend operator fun invoke(referencePosition: Position): List<NearbyStop> {
        val threshold = 0.005
        val maxLatitude: Double = referencePosition.latitude + threshold
        val minLatitude: Double = referencePosition.latitude - threshold
        val maxLongitude: Double = referencePosition.longitude + threshold
        val minLongitude: Double = referencePosition.longitude - threshold

        return stopRepository.obtainStops()
            .filter { stop -> stop.position.manhattanDistance(referencePosition) <= threshold }
            .sortedBy { stop -> stop.position.manhattanDistance(referencePosition) }
            .take(4)
            .map { stop -> NearbyStop(stop, haversineDistance(referencePosition, stop.position)) }
    }


}

private fun haversineDistance(pos1: Position, pos2: Position): Int {
    val R = 6371e3 // Earth radius in meters
    val lat1 = pos1.latitude.toRadians()
    val lat2 = pos2.latitude.toRadians()
    val deltaLat = (pos2.latitude - pos1.latitude).toRadians()
    val deltaLon = (pos2.longitude - pos1.longitude).toRadians()

    val a = sin(deltaLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(deltaLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return (R * c).toInt()
}

private fun Double.toRadians() = this * PI / 180
