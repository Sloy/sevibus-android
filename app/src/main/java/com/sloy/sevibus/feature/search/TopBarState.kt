package com.sloy.sevibus.feature.search

import com.sloy.sevibus.domain.model.Line
import com.sloy.sevibus.domain.model.LineSummary
import com.sloy.sevibus.domain.model.Stop

sealed interface TopBarState {
    data class Search(val term: String, val isSearchActive: Boolean) : TopBarState
    data class LineSelected(val line: Line) : TopBarState
    data class StopSelected(val stop: Stop) : TopBarState
}
