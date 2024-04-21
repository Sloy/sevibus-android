package com.sloydev.sevibus.domain

sealed interface SearchResult {
    data class LineResult(val line: Line) : SearchResult
    data class StopResult(val stop: Stop, val lines: List<Line>) : SearchResult
}