package com.sloydev.sevibus.feature.linestops

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloydev.sevibus.domain.model.LineId
import com.sloydev.sevibus.domain.repository.LineRepository
import com.sloydev.sevibus.domain.repository.StopRepository
import com.sloydev.sevibus.infrastructure.SevLogger
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class LineStopsViewModel(
    private val lineId: LineId,
    private val lineRepository: LineRepository,
    private val stopRepository: StopRepository,
) : ViewModel() {

    val state = MutableStateFlow<LineRouteScreenState>(LineRouteScreenState.Loading)
    var selectedTab by mutableIntStateOf(0)

    init {
        viewModelScope.launch {
            runCatching {
                val line = async { lineRepository.obtainLine(lineId) }
                val routeStops = async { stopRepository.obtainRouteStops(lineId) }

                state.value = LineRouteScreenState.Content.Partial(line.await())
                state.value = LineRouteScreenState.Content.Full(line.await(), routeStops.await())
            }.onFailure {
                state.value = LineRouteScreenState.Error
            }
        }
    }

    fun onTabSelected(index: Int) {
        selectedTab = index
    }

}
