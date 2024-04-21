package com.sloydev.sevibus.feature.linestops

import com.sloydev.sevibus.domain.Line
import com.sloydev.sevibus.domain.Stop

data class LineStopsScreenState(
    val line: Line,
    val directions: List<String>,
    val stops: List<Stop>
)