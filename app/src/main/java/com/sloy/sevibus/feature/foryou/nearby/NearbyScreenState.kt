package com.sloy.sevibus.feature.foryou.nearby

import com.sloy.sevibus.domain.model.Stop

sealed interface NearbyScreenState {
    data object Loading : NearbyScreenState
    data class Content(val stops: List<NearbyStop>) : NearbyScreenState
}

data class NearbyStop(val stop: Stop, val distance: Int)
