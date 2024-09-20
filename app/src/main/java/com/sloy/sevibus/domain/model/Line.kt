package com.sloy.sevibus.domain.model

import com.sloy.sevibus.Stubs

data class Line(
    val label: String,
    val description: String,
    val color: LineColor,
    val group: String = Stubs.groupFromLine(label),
    val routes: List<Route> = emptyList(),
    val id: LineId = 999,
)

typealias LineId = Int

data class LineSummary(
    val id: LineId,
    val label: String,
    val color: LineColor,
)

fun Line.toSummary() = LineSummary(id, label, color)