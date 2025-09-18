package com.sloy.sevibus.feature.debug.inappreview

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

class InAppReviewDebugModuleViewModel constructor(private val dataSource: InAppReviewDebugModuleDataSource) : ViewModel() {
    val state: StateFlow<InAppReviewDebugModuleState> = dataSource.observeCurrentState()

    fun onInAppReviewEnabledChanged(isEnabled: Boolean) {
        dataSource.updateState(state.value.copy(isInAppReviewEnabled = isEnabled))
    }
}