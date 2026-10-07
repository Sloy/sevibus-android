package com.sloy.sevibus.feature.stopdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloy.sevibus.domain.model.BusArrival
import com.sloy.sevibus.domain.model.FavoriteStop
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.domain.repository.BusRepository
import com.sloy.sevibus.domain.repository.FavoriteRepository
import com.sloy.sevibus.domain.repository.StopRepository
import com.sloy.sevibus.infrastructure.SevLogger
import com.sloy.sevibus.infrastructure.analytics.Analytics
import com.sloy.sevibus.infrastructure.analytics.events.Clicks
import com.sloy.sevibus.infrastructure.session.SessionService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class StopDetailViewModel(
    private val stopId: StopId,
    private val stopRepository: StopRepository,
    private val busRepository: BusRepository,
    private val favoriteRepository: FavoriteRepository,
    private val sessionService: SessionService,
    private val analytics: Analytics,
) : ViewModel() {

    private val favorite: Flow<FavoriteStop?> = favoriteRepository.observeFavorites()
        .map { it.find { favorite -> favorite.stop.code == stopId } }

    private val arrivals: Flow<Result<List<BusArrival>>> = flow {
        emit(Result.success(emptyList()))
        while (true) {
            try {
                val arrivals = busRepository.obtainBusArrivals(stopId)
                emit(Result.success(arrivals))
            } catch (e: Exception) {
                SevLogger.logW(e)
                emit(Result.failure(e))
            }
            delay(20.seconds)
        }
    }

    val events = MutableSharedFlow<StopDetailScreenEvent>()

    val state: StateFlow<StopDetailScreenState> = combine(
        flow { emit(stopRepository.obtainStop(stopId)) },
        favorite,
        arrivals
    ) { stop, isFavorite, arrivalsResult ->
        arrivalsResult.map { arrivals ->
            if (arrivals.isEmpty()) {
                StopDetailScreenState.Loaded(stop, isFavorite, ArrivalsState.Loading(stop.lines))
            } else {
                StopDetailScreenState.Loaded(stop, isFavorite, ArrivalsState.Loaded(arrivals))
            }
        }.recover {
            StopDetailScreenState.Loaded(stop, isFavorite, ArrivalsState.Failed(emptyList(), arrivalsResult.exceptionOrNull()!!))
        }.getOrThrow()
    }.catch { StopDetailScreenState.Failed(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(2000), StopDetailScreenState.Loading)

    fun onFavoriteClick() = viewModelScope.launch {
        if (sessionService.isLogged()) {
            (state.value as? StopDetailScreenState.Loaded)?.let { state ->
                if (state.favorite != null) {
                    favoriteRepository.removeFavorite(stopId)
                    analytics.track(Clicks.RemoveFavoriteClicked(stopId))
                } else {
                    favoriteRepository.addFavorite(FavoriteStop(state.stop, null, null))
                    analytics.track(Clicks.AddFavoriteClicked(stopId))
                }
            }
        } else {
            events.emit(StopDetailScreenEvent.LoginRequired(loginAction = { sessionService.manualSignIn(it) }))
        }
    }
}
