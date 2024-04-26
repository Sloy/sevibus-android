package com.sloydev.sevibus.feature.stopdetail

import com.sloydev.sevibus.domain.model.Stop

sealed interface StopDetailScreenState {
    data object Error : StopDetailScreenState
    data object Loading : StopDetailScreenState
    data class Content(
        val stop: Stop,
        //arrivals
    ): StopDetailScreenState
}