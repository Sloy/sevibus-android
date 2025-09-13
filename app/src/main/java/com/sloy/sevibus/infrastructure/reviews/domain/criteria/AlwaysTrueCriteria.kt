package com.sloy.sevibus.infrastructure.reviews.domain.criteria

import com.sloy.sevibus.infrastructure.analytics.SevEvent
import com.sloy.sevibus.infrastructure.reviews.domain.HappyMomentCriteria
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AlwaysTrueCriteria(
) : HappyMomentCriteria {

    override val name = "Always true"

    private val _happyMoment = MutableStateFlow(true)

    override fun observeHappyMoment(): StateFlow<Boolean> {
        return _happyMoment.asStateFlow()
    }

    override fun dispatch(event: SevEvent) {
    }
}