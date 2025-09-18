package com.sloy.sevibus.feature.debug.inappreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloy.sevibus.infrastructure.reviews.domain.InAppReviewHappyMomentService
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class InAppReviewDebugModuleViewModel constructor(
    private val dataSource: InAppReviewDebugModuleDataSource,
    private val inAppReviewService: InAppReviewHappyMomentService
) : ViewModel() {

    val state: StateFlow<InAppReviewDebugModuleState> = combine(
        dataSource.observeCurrentState(),
        inAppReviewService.observeActiveCriteriaName()
    ) { baseState, activeCriteriaName ->
        baseState.copy(activeCriteriaName = activeCriteriaName)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, dataSource.defaultValue)

    fun onInAppReviewEnabledChanged(isEnabled: Boolean) {
        dataSource.updateState(state.value.copy(isInAppReviewEnabled = isEnabled))
    }
}