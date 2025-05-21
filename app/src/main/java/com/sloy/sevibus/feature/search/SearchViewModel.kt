package com.sloy.sevibus.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloy.sevibus.domain.model.SearchResult
import com.sloy.sevibus.domain.repository.LineRepository
import com.sloy.sevibus.domain.repository.StopRepository
import com.sloy.sevibus.infrastructure.SevLogger
import com.sloy.sevibus.navigation.NavigationDestination
import com.sloy.sevibus.navigation.SevNavigator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

class SearchViewModel(
    private val stopsRepository: StopRepository,
    private val linesRepository: LineRepository,
    private val sevNavigator: SevNavigator,
) : ViewModel() {

    val searchTerm: MutableStateFlow<String> = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val results: StateFlow<List<SearchResult>> = searchTerm
        .mapLatest { term ->
            if (term.isBlank()) {
                emptyList()
            } else {
                searchLines(term).take(3) +
                        searchStops(term).take(100)
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
    }

    private suspend fun searchLines(term: String): List<SearchResult.LineResult> {
        return linesRepository.searchLines(term).map { SearchResult.LineResult(it) }
    }

    private suspend fun searchStops(term: String): List<SearchResult.StopResult> {
        return stopsRepository.searchStops(term).map { SearchResult.StopResult(it) }
    }
}
