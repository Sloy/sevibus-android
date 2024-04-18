package com.sloydev.sevibus.feature.lines

sealed interface LinesState {
    data object Loading : LinesState
    data class Content(val lines: List<Line>) : LinesState
}