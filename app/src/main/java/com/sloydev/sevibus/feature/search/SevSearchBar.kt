package com.sloydev.sevibus.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.domain.model.Line
import com.sloydev.sevibus.domain.model.SearchResult
import com.sloydev.sevibus.domain.model.Stop
import com.sloydev.sevibus.ui.components.LineIndicatorMedium
import com.sloydev.sevibus.ui.components.StopCardElement
import com.sloydev.sevibus.ui.theme.SevTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SevSearchBar(
    onSearchResultClicked: (SearchResult) -> Unit,
    modifier: Modifier = Modifier,
    defaultExpanded: Boolean = false,
    defaultText: String = ""
) {
    var text by rememberSaveable { mutableStateOf(defaultText) }
    var expanded by rememberSaveable { mutableStateOf(defaultExpanded) }
    DockedSearchBar(
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = modifier
            .fillMaxWidth(),
        inputField = {
            SearchBarDefaults.InputField(
                query = text,
                onQueryChange = { text = it },
                onSearch = { expanded = false },
                expanded = expanded,
                onExpandedChange = { expanded = it },
                placeholder = { Text("Busca líneas y paradas") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (expanded) {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(Icons.Default.KeyboardAlt, contentDescription = "Numeric keyboard")
                        }
                    }
                }
            )
        },
        expanded = expanded && text.isNotBlank(),
        onExpandedChange = { expanded = it },
    ) {
        if (text.isNotBlank()) {
            val results = Stubs.searchResults.take(9).drop(text.length)
            SearchResultsContent(results, onSearchResultClicked = {
                onSearchResultClicked(it)
                expanded = false
                text = ""
            })
        }
    }
}

@Composable
private fun SearchResultsContent(results: List<SearchResult>, onSearchResultClicked: (SearchResult) -> Unit) {
    Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {

        results.forEachIndexed { index, item ->
            when (item) {
                is SearchResult.LineResult -> LineResultItem(item.line, onLineClick = { onSearchResultClicked(item) })
                is SearchResult.StopResult -> StopResultItem(item.stop) { onSearchResultClicked(item) }
            }
        }

    }
}

@Composable
private fun LineResultItem(line: Line, onLineClick: (Line) -> Unit) {
    Card {
        ListItem(
            modifier = Modifier.clickable { onLineClick(line) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            headlineContent = { Text(line.description) },
            leadingContent = {
                LineIndicatorMedium(line)
            },
        )
    }
}

@Composable
private fun StopResultItem(stop: Stop, onStopClick: (Stop) -> Unit) {
    StopCardElement(stop = stop, onStopClick = onStopClick)
}

@Preview
@Composable
private fun Preview() {
    SevTheme {
        Column {
            SevSearchBar(onSearchResultClicked = {})
            Spacer(Modifier.size(32.dp))
            SevSearchBar(onSearchResultClicked = {}, defaultExpanded = true, defaultText = "macar")

        }
    }
}

@Preview
@Composable
private fun LinePreview() {
    SevTheme {
        Surface {
            LineResultItem(line = Stubs.lines[0], onLineClick = {})
        }
    }
}

@Preview
@Composable
private fun StopPreview() {
    SevTheme {
        Surface {
            StopResultItem(stop = Stubs.stops.random()) {}
        }
    }
}