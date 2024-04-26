package com.sloydev.sevibus.feature.stopdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloydev.sevibus.domain.model.StopId
import com.sloydev.sevibus.domain.repository.StopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class StopDetailViewModel(
    private val stopId: StopId,
    private val stopRepository: StopRepository
) : ViewModel() {

    val state = MutableStateFlow<StopDetailScreenState>(StopDetailScreenState.Loading)

    init {
        viewModelScope.launch {
            runCatching {
                val stop = stopRepository.obtainStop(stopId)
                state.value = StopDetailScreenState.Content(stop)
            }
        }
    }

}