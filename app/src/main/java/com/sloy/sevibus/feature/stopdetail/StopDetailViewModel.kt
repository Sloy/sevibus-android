package com.sloy.sevibus.feature.stopdetail

import android.os.SystemClock
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
import com.sloy.sevibus.infrastructure.analytics.events.Events
import com.sloy.sevibus.infrastructure.analytics.events.Screens
import com.sloy.sevibus.infrastructure.analytics.events.toArrivalsErrorType
import com.sloy.sevibus.infrastructure.analytics.events.trackLogin
import com.sloy.sevibus.infrastructure.session.SessionService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
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
    private val clock: () -> Long = SystemClock::elapsedRealtime,
) : ViewModel() {

    private var screenView: ScreenView? = null
    private var latestArrivals: List<BusArrival>? = null

    private val favorite: Flow<FavoriteStop?> = favoriteRepository.observeFavorites()
        .map { it.find { favorite -> favorite.stop.code == stopId } }

    private val arrivals: Flow<Result<List<BusArrival>?>> = flow {
        latestArrivals = null
        emit(Result.success(null))
        while (true) {
            try {
                val arrivals = busRepository.obtainBusArrivals(stopId)
                if (arrivals.isEmpty()) throw StopWithoutRoutesException(stopId)
                onArrivalsLoaded(arrivals)
                emit(Result.success(arrivals))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                SevLogger.logW(e)
                analytics.track(Events.ArrivalsFailed(Events.ArrivalsScreen.STOP_DETAIL, stopId, e.toArrivalsErrorType()))
                emit(Result.failure(e))
            }
            delay(20.seconds)
        }
    }.onCompletion { latestArrivals = null }

    val events = MutableSharedFlow<StopDetailScreenEvent>()

    val state: StateFlow<StopDetailScreenState> = combine(
        flow { emit(stopRepository.obtainStop(stopId)) },
        favorite,
        arrivals
    ) { stop, isFavorite, arrivalsResult ->
        arrivalsResult.map { arrivals ->
            if (arrivals == null) {
                StopDetailScreenState.Loaded(stop, isFavorite, ArrivalsState.Loading(stop.lines))
            } else {
                StopDetailScreenState.Loaded(stop, isFavorite, ArrivalsState.Loaded(arrivals))
            }
        }.recover {
            StopDetailScreenState.Loaded(stop, isFavorite, ArrivalsState.Failed(emptyList(), arrivalsResult.exceptionOrNull()!!))
        }.getOrThrow()
    }.catch { StopDetailScreenState.Failed(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(2000), StopDetailScreenState.Loading)

    /**
     * Starts a screen view: the screen is visible and the app is in foreground.
     */
    fun onScreenStarted() {
        screenView = ScreenView(startedAt = clock())
        latestArrivals?.let { trackArrivalsDisplayed(it) }
    }

    fun onScreenStopped() {
        val view = screenView ?: return
        screenView = null
        analytics.track(Screens.StopDetailsClosed(stopId, (clock() - view.startedAt) / 1000, view.refreshes))
    }

    fun onArrivalClick(arrival: BusArrival) {
        analytics.track(Clicks.ArrivalClicked(arrival.line.id, stopId))
    }

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
            analytics.track(Events.LoginPromptShown(Events.LoginTrigger.FAVORITE))
            events.emit(StopDetailScreenEvent.LoginRequired(loginAction = { context ->
                analytics.trackLogin(Events.LoginTrigger.FAVORITE) { sessionService.manualSignIn(context) }
            }))
        }
    }

    private fun onArrivalsLoaded(arrivals: List<BusArrival>) {
        latestArrivals = arrivals
        val view = screenView ?: return
        if (view.isArrivalsDisplayedTracked) {
            view.refreshes++
        } else {
            trackArrivalsDisplayed(arrivals)
        }
    }

    private fun trackArrivalsDisplayed(arrivals: List<BusArrival>) {
        val view = screenView ?: return
        if (view.isArrivalsDisplayedTracked) return
        view.isArrivalsDisplayedTracked = true
        analytics.track(
            Events.ArrivalsDisplayed(
                screen = Events.ArrivalsScreen.STOP_DETAIL,
                stopId = stopId,
                stopCount = null,
                arrivalsCount = arrivals.size,
                latencyMs = clock() - view.startedAt,
            )
        )
    }

    private class ScreenView(val startedAt: Long) {
        var isArrivalsDisplayedTracked = false
        var refreshes = 0
    }
}

private class StopWithoutRoutesException(stopId: StopId) : IllegalStateException("Stop $stopId has no routes")
