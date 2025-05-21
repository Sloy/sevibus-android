package com.sloy.sevibus.feature.foryou.nearby

import com.sloy.sevibus.domain.model.BusArrival

sealed class NearbyItemState(open val nearby: NearbyStop) {
    data class Loading(override val nearby: NearbyStop) : NearbyItemState(nearby)
    data class Loaded(override val nearby: NearbyStop, val arrivals: List<BusArrival>) : NearbyItemState(nearby)

}
