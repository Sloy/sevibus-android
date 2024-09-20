package com.sloy.sevibus.domain.model

sealed interface SearchResult {
    data class LineResult(val line: Line) : SearchResult
    data class StopResult(val stop: Stop) : SearchResult
}