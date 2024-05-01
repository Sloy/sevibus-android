package com.sloydev.sevibus.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloydev.sevibus.domain.model.Line
import com.sloydev.sevibus.domain.repository.LineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class LineSelectorViewModel(
    private val linesRepository: LineRepository,
) : ViewModel() {

    val lines = MutableStateFlow<List<Line>>(emptyList())

    init {
        viewModelScope.launch {
            lines.value = linesRepository.obtainLines()
        }
    }
}
