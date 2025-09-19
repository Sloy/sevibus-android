package com.sloy.sevibus.infrastructure.reviews.domain

import com.sloy.sevibus.feature.debug.inappreview.InAppReviewDebugModuleDataSource
import com.sloy.sevibus.infrastructure.SevLogger
import com.sloy.sevibus.infrastructure.analytics.SevEvent
import com.sloy.sevibus.infrastructure.reviews.domain.criteria.AddingFavoriteCriteria
import com.sloy.sevibus.infrastructure.reviews.domain.criteria.ReturningUserWithFavoritesCriteria
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Service that manages multiple happy moment criteria for A/B testing.
 *
 * This service coordinates different criteria implementations and provides
 * a unified interface for determining when to show in-app review prompts.
 * The actual logic for selecting which criteria to use is TBD and will be
 * implemented based on A/B testing requirements.
 */
class InAppReviewHappyMomentService(
    private val criteriaList: List<HappyMomentCriteria>,
    private val debugDataSource: InAppReviewDebugModuleDataSource
) {

    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val activeCriteria = MutableStateFlow<HappyMomentCriteria?>(null)

    /**
     * Exposes `true` when the active criteria's conditions are met and we should ask the user for a review.
     * The debug setting can override this to always return false when in-app reviews are disabled.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeHappyMoment(): StateFlow<Boolean> {
        val criteriaFlow = activeCriteria.flatMapLatest { criteria ->
            criteria?.observeHappyMoment() ?: flowOf(false)
        }

        return combine(
            criteriaFlow,
            debugDataSource.observeCurrentState()
        ) { criteriaResult, debugState ->
            criteriaResult && debugState.isInAppReviewEnabled
        }.stateIn(backgroundScope, SharingStarted.Eagerly, false)
    }

    /**
     * Exposes the name of the currently active criteria, or null if no criteria is active.
     */
    fun observeActiveCriteriaName(): StateFlow<String?> {
        return activeCriteria.map { criteria ->
            criteria?.name
        }.stateIn(backgroundScope, SharingStarted.Eagerly, null)
    }

    /**
     * Returns all available criteria for selection.
     */
    fun getAllCriteria(): List<HappyMomentCriteria> {
        return criteriaList
    }

    /**
     * Sets the active criteria by name. Used by debug module for testing different criteria.
     */
    fun setActiveCriteria(criteriaName: String) {
        val criteria = criteriaList.find { it.name == criteriaName }
        if (criteria != null) {
            activeCriteria.value = criteria
            SevLogger.logD(msg = "Active criteria changed to: $criteriaName (debug override)")
        } else {
            SevLogger.logW(msg = "Criteria not found: $criteriaName")
        }
    }

    /**
     * Reverts to live criteria selection logic, removing any debug override.
     */
    fun revertToLiveCriteria() {
        selectLiveCriteria()
        SevLogger.logD(msg = "Reverted to live criteria")
    }


    /**
     * Selects criteria using the live/production logic.
     * This function can be evolved for A/B testing and feature flags.
     */
    private fun selectLiveCriteria() {
        // For now, prefer the new AddingFavoriteCriteria, but fallback to ReturningUserWithFavoritesCriteria
        activeCriteria.value = criteriaList.find { it is AddingFavoriteCriteria }
            ?: criteriaList.find { it is ReturningUserWithFavoritesCriteria }
        SevLogger.logD("Selected live criteria: ${activeCriteria.value?.name}")
    }

    init {
        if (criteriaList.isEmpty()) {
            SevLogger.logW(msg = "No happy moment criteria found")
        } else {
            // Check if there's a debug criteria override
            val debugState = debugDataSource.observeCurrentState().value
            val debugCriteriaName = debugState.selectedDebugCriteriaName

            if (debugCriteriaName != null) {
                // Use debug override
                val debugCriteria = criteriaList.find { it.name == debugCriteriaName }
                if (debugCriteria != null) {
                    activeCriteria.value = debugCriteria
                    SevLogger.logD(msg = "Using debug criteria override: $debugCriteriaName")
                } else {
                    SevLogger.logW(msg = "Debug criteria not found: $debugCriteriaName, falling back to live")
                    selectLiveCriteria()
                }
            } else {
                selectLiveCriteria()
                SevLogger.logD(msg = "Using live criteria: ${activeCriteria.value?.name}")
            }
        }
    }

    /**
     * Dispatches an event to all criteria. Each criteria can choose to handle or ignore the event.
     */
    fun dispatch(event: SevEvent) {
        criteriaList.forEach { it.dispatch(event) }
    }
}