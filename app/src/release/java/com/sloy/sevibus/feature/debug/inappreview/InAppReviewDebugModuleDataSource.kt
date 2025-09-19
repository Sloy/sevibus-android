package com.sloy.sevibus.feature.debug.inappreview

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InAppReviewDebugModuleDataSource(context: Context) {
    fun observeCurrentState(): StateFlow<InAppReviewDebugModuleState> = MutableStateFlow(InAppReviewDebugModuleState())
}

data class InAppReviewDebugModuleState(
    val isInAppReviewEnabled: Boolean = true,
    val activeCriteriaName: String? = null,
    val availableCriteria: List<String> = emptyList(),
    val selectedDebugCriteriaName: String? = null,
)