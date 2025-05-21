package com.sloy.sevibus.feature.foryou.favorites

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloy.sevibus.domain.repository.BusRepository
import com.sloy.sevibus.domain.repository.FavoriteRepository
import com.sloy.sevibus.infrastructure.FeatureFlags
import com.sloy.sevibus.infrastructure.SevLogger
import com.sloy.sevibus.infrastructure.session.SessionService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesListViewModel(
    private val favoriteRepository: FavoriteRepository,
    private val busRepository: BusRepository,
    private val sessionService: SessionService,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<FavoritesListState> = sessionService.observeCurrentUser()
        .distinctUntilChanged()
        .flatMapLatest { user ->
            if (user == null && FeatureFlags.favoritesRequireUser) {
                flowOf<FavoritesListState>(FavoritesListState.NotLogged)
            } else {
                favoriteRepository.observeFavorites()
                    .map { FavoritesListState.Content(it) }
            }
        }
        .catch { SevLogger.logW(it, "Error obtaining favorites") }
        .stateIn(viewModelScope, SharingStarted.Lazily, FavoritesListState.Loading)

    val isLoginLoading = MutableStateFlow<Boolean>(false)

    fun onLoginClick(context: Context) {
        viewModelScope.launch {
            isLoginLoading.value = true
            sessionService.manualSignIn(context)
            //TODO show some error message if the login fails
            isLoginLoading.value = false
        }
    }

}
