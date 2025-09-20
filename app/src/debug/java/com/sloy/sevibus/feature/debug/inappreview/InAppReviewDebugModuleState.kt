package com.sloy.sevibus.feature.debug.inappreview

import kotlinx.serialization.Serializable

@Serializable
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
