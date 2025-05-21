package com.sloy.sevibus.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.Line
import com.sloy.sevibus.domain.model.SearchResult
import com.sloy.sevibus.domain.model.Stop
import com.sloy.sevibus.domain.model.description1
import com.sloy.sevibus.domain.model.description2
import com.sloy.sevibus.navigation.NavigationDestination
import com.sloy.sevibus.ui.components.LineElement
import com.sloy.sevibus.ui.icons.SevIcons
import com.sloy.sevibus.ui.icons.Stop
import com.sloy.sevibus.ui.preview.ScreenPreview
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
fun SearchScreen(results: List<SearchResult>, onNavigate: (NavigationDestination) -> Unit, modifier: Modifier = Modifier) {
    SearchResultsContent(results, modifier) { searchResult ->
        when (searchResult) {
            is SearchResult.LineResult -> onNavigate(NavigationDestination.LineStops(searchResult.line.id))
            is SearchResult.StopResult -> onNavigate(NavigationDestination.StopDetail(searchResult.stop.code))
        }
    }
}


@Composable
private fun SearchResultsContent(
    results: List<SearchResult>,
    modifier: Modifier = Modifier,
    onSearchResultClicked: (SearchResult) -> Unit
) {
    LazyColumn(
        modifier.padding(vertical = 8.dp, horizontal = 16.dp)
    ) {

        // Lines
        val lines = results.filterIsInstance<SearchResult.LineResult>()
        if (lines.isNotEmpty()) {
            item { Text("Líneas", style = SevTheme.typography.headingSmall, modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)) }
        }
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(shape = SevTheme.shapes.large)
                    .background(SevTheme.colorScheme.surface)
                    .padding(vertical = 0.dp)

            ) {
                lines.forEachIndexed { index, lineResult ->
                    LineResultItem(lineResult.line) { onSearchResultClicked(lineResult) }
                    if (index < lines.size - 1) {
                        HorizontalDivider()
                    }
                }
            }
        }

        // Stops
        val stops = results.filterIsInstance<SearchResult.StopResult>()
        if (stops.isNotEmpty()) {
            item { Text("Paradas", style = SevTheme.typography.headingSmall, modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)) }
        }
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(shape = SevTheme.shapes.large)
                    .background(SevTheme.colorScheme.surface)
                    .padding(vertical = 0.dp)

            ) {
                stops.forEachIndexed { index, stopResult ->
                    StopResultItem(stopResult.stop) { onSearchResultClicked(stopResult) }
                    if (index < stops.size - 1) {
                        HorizontalDivider()
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(WindowInsets.ime.asPaddingValues().calculateBottomPadding()))
        }

    }
}

@Composable
private fun LineResultItem(line: Line, onLineClick: (Line) -> Unit) {
    LineElement(line, onLineClick)
}

@Composable
private fun StopResultItem(stop: Stop, onStopClick: (Stop) -> Unit) {
    Row(
        Modifier
            .padding(16.dp)
            .clickable { onStopClick(stop) }) {
        Icon(
            SevIcons.Stop,
            contentDescription = null,
            tint = SevTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 16.dp)
        )
        Column(Modifier.weight(1f)) {
            Text(
                stop.description1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = SevTheme.typography.bodyStandard
            )
            stop.description2?.let {
                Text(
                    it,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = SevTheme.typography.bodySmall,
                    color = SevTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Parada ${stop.code}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = SevTheme.typography.bodyExtraSmall,
                color = SevTheme.colorScheme.onSurfaceVariant
            )

        }
    }

}

@Preview
@Composable
private fun PreviewResults() {
    ScreenPreview {
        SearchScreen(Stubs.searchResults, {})
    }
}

@Preview
@Composable
private fun PreviewEmpty() {
    ScreenPreview {
        SearchScreen(emptyList(), {})
    }
}
