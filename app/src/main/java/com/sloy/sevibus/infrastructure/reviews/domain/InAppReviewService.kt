package com.sloy.sevibus.infrastructure.reviews.domain

import com.sloy.sevibus.feature.debug.inappreview.InAppReviewDebugModuleDataSource
import com.sloy.sevibus.infrastructure.SevLogger
import com.sloy.sevibus.infrastructure.experimentation.Experiment
import com.sloy.sevibus.infrastructure.experimentation.Experiments
import com.sloy.sevibus.infrastructure.experimentation.FeatureFlag
import com.sloy.sevibus.infrastructure.reviews.domain.criteria.AlwaysFalseCriteria
import com.sloy.sevibus.infrastructure.reviews.domain.criteria.AlwaysTrueCriteria
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn


/**
 * Service that manages multiple happy moment criteria for A/B testing.
 *
 * This service coordinates different criteria implementations and provides
 * a unified interface for determining when to show in-app review prompts.
 * The actual logic for selecting which criteria to use is TBD and will be
 * implemented based on A/B testing requirements.
 */
class InAppReviewService(
    private val criteriaList: List<HappyMomentCriteria>,
    private val debugDataSource: InAppReviewDebugModuleDataSource,
    private val experiments: Experiments,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val activeCriteria = MutableStateFlow<HappyMomentCriteria?>(null)

    init {
        debugCriteria().onEach { debugCriteria ->
            if (debugCriteria != null) {
                activeCriteria.value = debugCriteria
                SevLogger.logD(msg = "Using debug criteria: ${debugCriteria.name}")
            } else {
                activeCriteria.value = liveCriteria()
                SevLogger.logD(msg = "Using live criteria: ${activeCriteria.value?.name}")
            }
        }.launchIn(scope)
    }

    /**
     * Exposes `true` when the active criteria's conditions are met and we should ask the user for a review.
     * The debug setting can override this to always return false when in-app reviews are disabled.
     * The feature flag can disable the entire feature remotely.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeHappyMoment(): StateFlow<Boolean> {
        val criteriaValueFlow = activeCriteria.flatMapLatest { criteria ->
            criteria?.observeHappyMoment() ?: flowOf(false)
        }
        return criteriaValueFlow.distinctUntilChanged().stateIn(scope, SharingStarted.Eagerly, false)
    }

    internal fun observeActiveCriteria(): StateFlow<HappyMomentCriteria?> {
        return activeCriteria.stateIn(scope, SharingStarted.Eagerly, null)
    }

    internal fun getAllCriteria(): List<HappyMomentCriteria> {
        return criteriaList
    }

    private suspend fun liveCriteria(): HappyMomentCriteria {
        return experimentCriteria() ?: if (experiments.isFeatureEnabled(FeatureFlag.IN_APP_REVIEWS)) {
            AlwaysTrueCriteria()
        } else {
            AlwaysFalseCriteria()
        }
    }

    private suspend fun experimentCriteria(): HappyMomentCriteria? {
        val experimentResult = experiments.getExperiment(Experiment.InAppReviewsCriteria)
        val criteriaName = experimentResult.parameters[INAPP_EXPERIMENT_PARAMETER] as? String ?: return null
        return criteriaList.find { it.name == criteriaName }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun debugCriteria(): Flow<HappyMomentCriteria?> {
        return debugDataSource.observeCurrentState()
            .map { it.debugCriteria }
            .flatMapLatest { debugCriteriaName ->
                flowOf(criteriaList.find { it.name == debugCriteriaName })
            }
    }

}

private const val INAPP_EXPERIMENT_PARAMETER = "Happy Moment criteria"