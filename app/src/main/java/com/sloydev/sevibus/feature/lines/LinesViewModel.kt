package com.sloydev.sevibus.feature.lines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloydev.sevibus.domain.repository.LineRepository
import com.sloydev.sevibus.infrastructure.SevLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class LinesViewModel(
    private val repository: LineRepository
) : ViewModel() {

    val state = MutableStateFlow<LinesScreenState>(LinesScreenState.Loading)

    init {
        viewModelScope.launch {
            repository.obtainLines()
                .onSuccess { lines ->
                    val groupsOfLines = lines
                        .groupBy { it.group }
                        .map { (group, linesForGroup) -> LinesScreenState.Content.GroupOfLines(group, linesForGroup) }
                    state.value = LinesScreenState.Content(groupsOfLines)
                }
                .onFailure {
                    state.value = LinesScreenState.Error
                    SevLogger.logW(it)
                }
        }
    }

}