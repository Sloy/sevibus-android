package com.sloy.sevibus.feature.debug.inappreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloy.sevibus.domain.repository.FavoriteRepository
import com.sloy.sevibus.infrastructure.reviews.domain.AppStartTrackingDataSource
import com.sloy.sevibus.infrastructure.reviews.domain.InAppReviewHappyMomentService
import com.sloy.sevibus.infrastructure.session.SessionService
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class InAppReviewDebugModuleViewModel(
    private val dataSource: InAppReviewDebugModuleDataSource,
    private val inAppReviewService: InAppReviewHappyMomentService,
    private val favoriteRepository: FavoriteRepository,
    private val sessionService: SessionService,
    private val appStartTracker: AppStartTrackingDataSource
) : ViewModel() {

    val state: StateFlow<InAppReviewDebugModuleState> = combine(
        dataSource.observeCurrentState(),
        inAppReviewService.observeActiveCriteriaName(),
        favoriteRepository.observeFavorites().map { it.size },
        appStartTracker.observeAppStartCount(),
        sessionService.observeCurrentUser().map { it != null }
    ) { baseState, activeCriteriaName, favoritesCount, appOpensCount, isUserLoggedIn ->
        baseState.copy(
            activeCriteriaName = activeCriteriaName,
            availableCriteria = inAppReviewService.getAllCriteria().map { it.name },
            favoritesCount = favoritesCount,
            appOpensCount = appOpensCount,
            isUserLoggedIn = isUserLoggedIn
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, dataSource.defaultValue)

    fun onInAppReviewEnabledChanged(isEnabled: Boolean) {
        dataSource.updateState(state.value.copy(isInAppReviewEnabled = isEnabled))
    }

    fun onCriteriaSelected(criteriaName: String) {
        // Store the debug criteria selection
        dataSource.updateState(state.value.copy(selectedDebugCriteriaName = criteriaName))
        // Update the service to use the debug criteria
        inAppReviewService.setActiveCriteria(criteriaName)
    }

    fun onRevertToLiveCriteria() {
        // Clear the debug criteria selection
        dataSource.updateState(state.value.copy(selectedDebugCriteriaName = null))
        // Tell the service to revert to live criteria
        inAppReviewService.revertToLiveCriteria()
    }
}