package com.sloy.sevibus.feature.foryou.favorites.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloy.sevibus.domain.model.FavoriteStop
import com.sloy.sevibus.domain.repository.FavoriteRepository
import com.sloy.sevibus.infrastructure.SevLogger
import com.sloy.sevibus.infrastructure.session.SessionService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EditFavoritesViewModel(
    private val favoriteRepository: FavoriteRepository,
    private val sessionService: SessionService,
) : ViewModel() {

    val state = favoriteRepository.observeFavorites()
        .map { EditFavoritesState(it) }
        .catch { SevLogger.logW(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, EditFavoritesState(emptyList()))

    val events = MutableSharedFlow<EditFavoritesEvent>()

    fun onFavoritesChanged(favorites: List<FavoriteStop>) {
        viewModelScope.launch {
            favoriteRepository.replaceFavorites(favorites)
            events.emit(EditFavoritesEvent.Done)
        }
    }
}

data class EditFavoritesState(val favorites: List<FavoriteStop>)

sealed class EditFavoritesEvent {
    object Done : EditFavoritesEvent()
    //data class Error(val error: Throwable) : EditFavoritesEvent()
}
