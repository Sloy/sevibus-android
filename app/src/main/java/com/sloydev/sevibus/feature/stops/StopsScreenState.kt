package com.sloydev.sevibus.feature.stops

import com.sloydev.sevibus.feature.lines.Line

data class StopsScreenState(
    val line: Line,
    val directions: List<String>,
    val stops: List<Stop>
)