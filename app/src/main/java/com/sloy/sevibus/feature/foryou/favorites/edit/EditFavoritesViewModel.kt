package com.sloy.sevibus.feature.foryou.favorites.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloy.sevibus.domain.model.FavoriteStop
import com.sloy.sevibus.domain.repository.FavoriteRepository
import com.sloy.sevibus.infrastructure.SevLogger
import com.sloy.sevibus.infrastructure.analytics.Analytics
import com.sloy.sevibus.infrastructure.analytics.SevEvent
import com.sloy.sevibus.infrastructure.analytics.events.Events
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
    private val analytics: Analytics,
) : ViewModel() {

    val state = favoriteRepository.observeFavorites()
        .map { EditFavoritesState(it) }
        .catch { SevLogger.logW(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, EditFavoritesState(emptyList()))

    val events = MutableSharedFlow<EditFavoritesEvent>()

    private var isSaved = false

    fun onScreenOpened() {
        isSaved = false
    }

    fun onScreenClosed() {
        if (!isSaved) analytics.track(Events.EditFavoritesCancelled)
    }

    fun onFavoritesChanged(favorites: List<FavoriteStop>) {
        isSaved = true
        analytics.track(favoritesDiff(state.value.favorites, favorites))
        viewModelScope.launch {
            favoriteRepository.replaceFavorites(favorites)
            events.emit(EditFavoritesEvent.Done)
        }
    }

    fun track(event: SevEvent) {
        analytics.track(event)
    }
}

fun favoritesDiff(old: List<FavoriteStop>, new: List<FavoriteStop>): Events.EditFavoritesSaved {
    val oldByStop = old.associateBy { it.stop.code }
    val newStops = new.map { it.stop.code }.toSet()
    val kept = new.mapNotNull { favorite -> oldByStop[favorite.stop.code]?.let { previous -> previous to favorite } }
    return Events.EditFavoritesSaved(
        renamed = kept.count { (previous, current) -> previous.customName != current.customName },
        iconChanged = kept.count { (previous, current) -> previous.customIcon != current.customIcon },
        deleted = old.count { it.stop.code !in newStops },
        linesChanged = kept.count { (previous, current) -> previous.selectedLineIds != current.selectedLineIds },
        reordered = kept.map { (_, current) -> current.stop.code } != old.map { it.stop.code }.filter { it in newStops },
    )
}

data class EditFavoritesState(val favorites: List<FavoriteStop>)

sealed class EditFavoritesEvent {
    object Done : EditFavoritesEvent()
    //data class Error(val error: Throwable) : EditFavoritesEvent()
}
