package com.sloy.sevibus.feature.debug.inappreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloy.sevibus.domain.repository.FavoriteRepository
import com.sloy.sevibus.infrastructure.experimentation.Experiment
import com.sloy.sevibus.infrastructure.experimentation.Experiments
import com.sloy.sevibus.infrastructure.experimentation.FeatureFlag
import com.sloy.sevibus.infrastructure.reviews.domain.AppStartTrackingDataSource
import com.sloy.sevibus.infrastructure.reviews.domain.InAppReviewService
import com.sloy.sevibus.infrastructure.session.SessionService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class InAppReviewDebugModuleViewModel(
    private val debugDataSource: InAppReviewDebugModuleDataSource,
    private val inAppReviewService: InAppReviewService,
    private val favoriteRepository: FavoriteRepository,
    private val sessionService: SessionService,
    private val appStartTracker: AppStartTrackingDataSource,
    private val experiments: Experiments,
) : ViewModel() {

    val state: StateFlow<InAppReviewDebugModuleState> = combine(
        debugDataSource.observeCurrentState(),
        inAppReviewService.observeActiveCriteria(),
        favoriteRepository.observeFavorites().map { it.size },
        appStartTracker.observeAppStartCount(),
        sessionService.observeCurrentUser().map { it != null }
    ) { baseState, activeCriteria, favoritesCount, appOpensCount, isUserLoggedIn ->
        baseState.copy(
            experimentVariant = experiments.getExperiment(Experiment.InAppReviewsCriteria).parameters["Happy Moment criteria"] as? String,
            featureFlag = experiments.isFeatureEnabled(FeatureFlag.IN_APP_REVIEWS),
            activeCriteria = activeCriteria?.name,
            availableCriteria = inAppReviewService.getAllCriteria().map { it.name },
            favoritesCount = favoritesCount,
            appOpensCount = appOpensCount,
            isUserLoggedIn = isUserLoggedIn
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, debugDataSource.defaultValue)

    fun onCriteriaSelected(criteriaName: String) {
        debugDataSource.updateState(state.value.copy(debugCriteria = criteriaName))
    }

    fun onRevertToLiveCriteria() {
        debugDataSource.updateState(state.value.copy(debugCriteria = null))
    }
}