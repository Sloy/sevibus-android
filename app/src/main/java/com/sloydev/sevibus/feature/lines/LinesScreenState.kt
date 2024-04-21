package com.sloydev.sevibus.feature.lines

import com.sloydev.sevibus.domain.Line

sealed interface LinesScreenState {
    data object Loading : LinesScreenState
    data class Content(val lines: List<Line>) : LinesScreenState
}