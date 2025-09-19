package com.sloy.sevibus.infrastructure.experimentation

import com.statsig.androidsdk.DynamicConfig

data class ExperimentResult(
    val name: String,
    val parameters: Map<String, Any>,
    val isUserInExperiment: Boolean,
    val isExperimentActive: Boolean,
)

fun DynamicConfig.toExperimentResult() = ExperimentResult(
    name = getName(),
    isUserInExperiment = getIsUserInExperiment(),
    isExperimentActive = getIsExperimentActive(),
    parameters = getValue()
)