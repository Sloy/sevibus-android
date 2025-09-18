package com.sloy.sevibus.feature.debug.inappreview

import kotlinx.serialization.Serializable

@Serializable
data class InAppReviewDebugModuleState(
    val isInAppReviewEnabled: Boolean = true,
    val activeCriteriaName: String? = null,
)