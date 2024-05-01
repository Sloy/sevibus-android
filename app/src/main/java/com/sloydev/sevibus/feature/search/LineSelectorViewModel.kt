package com.sloydev.sevibus.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloydev.sevibus.domain.model.Line
import com.sloydev.sevibus.domain.repository.LineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LineSelectorViewModel(
    private val linesRepository: LineRepository,
) : ViewModel() {

    val state = MutableStateFlow(LineSelectorState(emptyList(), null))

    init {
        viewModelScope.launch {
            state.value = LineSelectorState(linesRepository.obtainLines(), null)
        }
    }

    fun onSelectLine(line: Line) {
        state.update { it.copy(lineSelected = line) }
    }
}

data class LineSelectorState(val lines: List<Line>, val lineSelected: Line?)