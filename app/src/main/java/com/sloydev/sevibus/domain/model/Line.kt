package com.sloydev.sevibus.domain.model

import com.sloydev.sevibus.Stubs
import java.time.LocalTime

data class Line(
    val label: String,
    val description: String,
    @Deprecated("We'll use hardcoded colors for now") val colorHex: Long,
    //TODO define colorScheme mapping
    val id: Int = 999,
    val destinations: List<Destination> = emptyList(),
    val group: String = Stubs.groupFromLine(label),
) {

    data class Destination(
        val direction: Int,
        val destination: String,
        val startTime: LocalTime,
        val endTime: LocalTime,
    )


}