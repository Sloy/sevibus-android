package com.sloy.sevibus.infrastructure.reviews.domain.criteria

import com.sloy.sevibus.infrastructure.analytics.SevEvent
import com.sloy.sevibus.infrastructure.reviews.domain.HappyMomentCriteria
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AlwaysFalseCriteria(
) : HappyMomentCriteria {

    override val name = "Always false"

    private val _happyMoment = MutableStateFlow(false)

    override fun observeHappyMoment(): StateFlow<Boolean> {
        return _happyMoment.asStateFlow()
    }

    override fun dispatch(event: SevEvent) {
    }
}