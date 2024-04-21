package com.sloydev.sevibus.feature.linestops

import com.sloydev.sevibus.domain.model.Line
import com.sloydev.sevibus.domain.model.Stop

data class LineStopsScreenState(
    val line: Line,
    val directions: List<String>,
    val stops: List<Stop>
)