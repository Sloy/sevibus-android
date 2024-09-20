package com.sloy.sevibus.domain.model

data class Stop(
    val code: StopId,
    val description: String,
    val position: Position,
    val lines: List<LineSummary>,
)

inline val Stop.description1: String
    get() = description.substringBeforeLast("(").trim()

val Stop.description2: String?
    get() {
        val description = description.substringAfterLast("(", "").substringBeforeLast(")", "").trim()
        return description.ifEmpty { null }
    }

fun Stop.descriptionSeparator(separator: String = "•"): String {
    return if (description2 != null) {
        "$description1 $separator $description2"
    } else {
        description1
    }
}

fun Collection<Stop>.filterInBounds(bounds: PositionBounds?): List<Stop> {
    if (bounds == null) return emptyList()
    return filter { stop ->
        stop.position.latitude <= bounds.northeast.latitude &&
                stop.position.latitude >= bounds.southwest.latitude &&
                stop.position.longitude <= bounds.northeast.longitude &&
                stop.position.longitude >= bounds.southwest.longitude
    }
}

typealias StopId = Int
