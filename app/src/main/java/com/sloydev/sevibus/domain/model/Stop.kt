package com.sloydev.sevibus.domain.model

import com.google.android.gms.maps.model.LatLng

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

typealias StopId = Int
