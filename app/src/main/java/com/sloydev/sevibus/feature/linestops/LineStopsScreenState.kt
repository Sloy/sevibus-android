package com.sloydev.sevibus.feature.linestops

import com.sloydev.sevibus.feature.lines.Line

data class LineStopsScreenState(
    val line: Line,
    val directions: List<String>,
    val stops: List<Stop>
)