package com.sloy.sevibus.feature.debug.inappreview

import kotlinx.serialization.Serializable

@Serializable
data class InAppReviewDebugModuleState(
    val experimentVariant: String? = null,
    val featureFlag: Boolean? = null,
    val activeCriteria: String? = null,
    // Debug builds never ask for a review unless a criteria is picked in the debug menu
    val debugCriteria: String? = "Always false",
    val availableCriteria: List<String> = emptyList(),
    val favoritesCount: Int = 0,
    val appOpensCount: Int = 0,
    val isUserLoggedIn: Boolean = false,
)
