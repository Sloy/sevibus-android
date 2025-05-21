package com.sloy.sevibus.feature.lines

import com.sloy.sevibus.domain.model.Line

sealed interface LinesScreenState {
    data object Loading : LinesScreenState
    data object Error : LinesScreenState
    data class Content(val lineGroups: List<GroupOfLines>) : LinesScreenState {
        data class GroupOfLines(val type: String, val lines: List<Line>)
    }
}