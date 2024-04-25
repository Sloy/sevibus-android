package com.sloydev.sevibus.domain.model

import com.sloydev.sevibus.Stubs

data class Line(
    val label: String,
    val description: String,
    val colorHex: Long,
    val group: String = Stubs.groupFromLine(label),
    val routes: List<Route> = emptyList(),
    val id: LineId = 999,
)

typealias LineId = Int

data class LineSummary(
    val id: LineId,
    val label: String,
    val colorHex: Long,
)

fun Line.toSummary() = LineSummary(id, label, colorHex)