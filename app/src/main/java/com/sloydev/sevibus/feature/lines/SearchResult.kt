package com.sloydev.sevibus.feature.lines

import com.sloydev.sevibus.feature.linestops.Stop

sealed interface SearchResult {
    data class LineResult(val line: Line) : SearchResult
    data class StopResult(val stop: Stop, val lines: List<Line>) : SearchResult
}