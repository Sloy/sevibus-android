package com.sloy.sevibus.feature.search

import androidx.lifecycle.ViewModel
import com.sloy.sevibus.domain.model.SearchResult
import com.sloy.sevibus.domain.repository.LineRepository
import com.sloy.sevibus.domain.repository.StopRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext

class SearchViewModel(
    private val stopsRepository: StopRepository,
    private val linesRepository: LineRepository,
) : ViewModel() {

    val searchTerm = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val results: Flow<List<SearchResult>> = searchTerm.mapLatest { term ->
        searchLines(term) //+ searchStops(term) //TODO stop search is too inefficient, needs a better implementation
    }

    fun onSearch(term: String) {
        searchTerm.value = term
    }

    private suspend fun searchLines(term: String): List<SearchResult.LineResult> {
        return withContext(Dispatchers.Default) {
            linesRepository.obtainLines().filter { line ->
                line.description.contains(term, ignoreCase = true) or
                        line.routes.any { it.destination.contains(term, ignoreCase = true) } or
                        line.label.contains(term, ignoreCase = true) or
                        line.group.contains(term, ignoreCase = true)
            }.map { SearchResult.LineResult(it) }
        }
    }

    private suspend fun searchStops(term: String): List<SearchResult.StopResult> {
        return withContext(Dispatchers.Default) {
            stopsRepository.obtainStops().filter { stop ->
                stop.code.toString().contains(term, ignoreCase = true) or
                        stop.description.contains(term, ignoreCase = true)
            }.map { SearchResult.StopResult(it) }
        }
    }
}