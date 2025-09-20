package com.sloy.sevibus.feature.debug.inappreview

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InAppReviewDebugModuleDataSource(context: Context) {
    fun observeCurrentState(): StateFlow<InAppReviewDebugModuleState> = MutableStateFlow(InAppReviewDebugModuleState())
}

data class InAppReviewDebugModuleState(
    val experimentVariant: String? = null,
    val featureFlag: Boolean? = null,
    val activeCriteria: String? = null,
    val debugCriteria: String? = null,
    val availableCriteria: List<String> = emptyList(),
    val favoritesCount: Int = 0,
    val appOpensCount: Int = 0,
    val isUserLoggedIn: Boolean = false,
)