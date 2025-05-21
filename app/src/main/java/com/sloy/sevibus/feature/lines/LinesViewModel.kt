package com.sloy.sevibus.feature.lines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloy.sevibus.domain.repository.LineRepository
import com.sloy.sevibus.infrastructure.SevLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class LinesViewModel(
    private val repository: LineRepository
) : ViewModel() {

    val state = MutableStateFlow<LinesScreenState>(LinesScreenState.Loading)

    init {
        viewModelScope.launch {
            runCatching {
                val lines = repository.obtainLines()
                val groupsOfLines = lines
                    .groupBy { it.group }
                    .map { (group, linesForGroup) -> LinesScreenState.Content.GroupOfLines(group, linesForGroup) }
                state.value = LinesScreenState.Content(groupsOfLines)
            }.onFailure {
                state.value = LinesScreenState.Error
                SevLogger.logW(it)
            }
        }
    }

}