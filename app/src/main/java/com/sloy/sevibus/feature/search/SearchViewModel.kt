package com.sloy.sevibus.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloy.sevibus.domain.model.SearchResult
import com.sloy.sevibus.domain.repository.LineRepository
import com.sloy.sevibus.domain.repository.StopRepository
import com.sloy.sevibus.infrastructure.SevLogger
import com.sloy.sevibus.infrastructure.analytics.Analytics
import com.sloy.sevibus.infrastructure.analytics.events.Events
import com.sloy.sevibus.navigation.NavigationDestination
import com.sloy.sevibus.navigation.SevNavigator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

class SearchViewModel(
    private val stopsRepository: StopRepository,
    private val linesRepository: LineRepository,
    private val sevNavigator: SevNavigator,
    private val analytics: Analytics,
) : ViewModel() {

    val searchTerm: MutableStateFlow<String> = MutableStateFlow("")
    private val typedTerm = MutableStateFlow("")

    init {
        trackSettledSearches()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val results: StateFlow<List<SearchResult>> = searchTerm
        .mapLatest { term ->
            if (term.isBlank()) {
                emptyList()
            } else {
                searchLines(term).take(MAX_LINE_RESULTS) +
                        searchStops(term).take(MAX_STOP_RESULTS)
            }
        }.catch { SevLogger.logE(it, "Error searching") }
        .stateIn(viewModelScope, started = SharingStarted.Lazily, initialValue = emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val topBarState = sevNavigator.observeDestination().combine(searchTerm) { destination, term ->
        if (destination is NavigationDestination.LineStops) {
            val line = linesRepository.obtainLine(destination.lineId)
            searchTerm.value = line.description
            TopBarState.LineSelected(line)
        } else if (destination is NavigationDestination.StopDetail) {
            val stop = stopsRepository.obtainStop(destination.stopId)
            searchTerm.value = stop.description
            TopBarState.StopSelected(stop)
        } else {
            if (destination != NavigationDestination.Search) {
                searchTerm.value = ""
            }
            TopBarState.Search(term, isSearchActive = destination == NavigationDestination.Search)
        }
    }.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = TopBarState.Search("", false))

    fun onSearch(term: String) {
        searchTerm.value = term
        typedTerm.value = term
    }

    @OptIn(FlowPreview::class)
    private fun trackSettledSearches() {
        typedTerm
            .debounce(SEARCH_SETTLE_TIME)
            .map { it.trim() }
            .distinctUntilChanged()
            .filter { it.isNotBlank() }
            .onEach { term ->
                val lineResults = searchLines(term).take(MAX_LINE_RESULTS).size
                val stopResults = searchStops(term).take(MAX_STOP_RESULTS).size
                val resultsCount = lineResults + stopResults
                analytics.track(
                    Events.SearchPerformed(
                        queryLength = term.length,
                        resultsCount = resultsCount,
                        stopResults = stopResults,
                        lineResults = lineResults,
                        query = if (resultsCount == 0) term.lowercase().take(MAX_TRACKED_QUERY_LENGTH) else null,
                    )
                )
            }
            .catch { SevLogger.logE(it, "Error tracking search") }
            .launchIn(viewModelScope)
    }

    private suspend fun searchLines(term: String): List<SearchResult.LineResult> {
        return linesRepository.searchLines(term).map { SearchResult.LineResult(it) }
    }

    private suspend fun searchStops(term: String): List<SearchResult.StopResult> {
        return stopsRepository.searchStops(term).map { SearchResult.StopResult(it) }
    }
}

private const val MAX_LINE_RESULTS = 3
private const val MAX_STOP_RESULTS = 100
private const val MAX_TRACKED_QUERY_LENGTH = 40
private val SEARCH_SETTLE_TIME = 1.seconds
