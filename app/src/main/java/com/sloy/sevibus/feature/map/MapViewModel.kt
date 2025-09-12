package com.sloy.sevibus.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloy.sevibus.domain.repository.StopRepository
import com.sloy.sevibus.feature.map.states.OnLineSelectedState
import com.sloy.sevibus.feature.map.states.OnLinesSectionSelected
import com.sloy.sevibus.feature.map.states.OnStopAndLineSelected
import com.sloy.sevibus.feature.map.states.OnStopSelectedState
import com.sloy.sevibus.infrastructure.SevLogger
import com.sloy.sevibus.infrastructure.analytics.Analytics
import com.sloy.sevibus.infrastructure.analytics.SevEvent
import com.sloy.sevibus.navigation.NavigationDestination
import com.sloy.sevibus.navigation.SevNavigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModel(
    private val sevNavigator: SevNavigator,
    private val stopRepository: StopRepository,
    private val onLineSelectedState: OnLineSelectedState,
    private val onStopSelectedState: OnStopSelectedState,
    private val onStopAndLineSelected: OnStopAndLineSelected,
    private val onLinesSectionSelected: OnLinesSectionSelected,
    private val analytics: Analytics,
) : ViewModel() {

    val state = sevNavigator.observeDestination().distinctUntilChanged().transformLatest { destination ->
        emitAll(mapDestination(destination))
    }.retryWhen { cause, attempt ->
        if (attempt > 5) {
            false
        } else {
            SevLogger.logW(cause, "Error while loading map state, retrying attempt $attempt")
            events.emit(MapScreenEvent.Error("Parece que hay algún error de conexión"))
            delay(attempt * 1000)
            true
        }
    }.catch { e ->
        SevLogger.logE(e, "Error while loading map state")
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = MapScreenState.Initial)

    val events = MutableSharedFlow<MapScreenEvent>()

    fun onTrack(event: SevEvent) {
        analytics.track(event)
    }

    private fun mapDestination(destination: NavigationDestination): Flow<MapScreenState> = flow {
        when (destination) {
            is NavigationDestination.ForYou -> onIdle()
            is NavigationDestination.Lines -> onLinesSectionSelected()
            is NavigationDestination.LineStops ->
                if (destination.highlightedStop == null) {
                    onLineSelectedState(
                        destination.lineId,
                        destination.routeId
                    )
                } else {
                    onStopAndLineSelected(destination.highlightedStop, destination.lineId)
                }

            is NavigationDestination.StopDetail ->
                if (destination.highlightedLine == null) {
                    onStopSelectedState(destination.stopId)
                } else {
                    onStopAndLineSelected(
                        destination.stopId,
                        destination.highlightedLine
                    )
                }

            else -> emptyFlow()
        }.also { emitAll(it) }
    }

    private fun onIdle(): Flow<MapScreenState> = flow {
        val stops = stopRepository.obtainStops()
        emit(MapScreenState.Idle(stops))
    }

}
