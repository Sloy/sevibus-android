package com.sloydev.sevibus.feature.lines

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import com.sloydev.sevibus.feature.linestops.Stop
import com.sloydev.sevibus.ui.components.LineIndicatorMedium
import com.sloydev.sevibus.ui.components.LineIndicatorSmall
import com.sloydev.sevibus.ui.theme.SevTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SbSearchBar(defaultExpanded: Boolean = false, defaultText: String = "") {
    var text by rememberSaveable { mutableStateOf(defaultText) }
    var expanded by rememberSaveable { mutableStateOf(defaultExpanded) }
    DockedSearchBar(
        modifier = Modifier
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
            SearchResultsContent(results)
        }
    }
}

@Composable
private fun SearchResultsContent(results: List<SearchResult>) {

    results.forEachIndexed { index, item ->
        when (item) {
            is SearchResult.LineResult -> LineResultItem(item.line, onLineClick = {})
            is SearchResult.StopResult -> StopResultItem(item.stop, item.lines, onStopClick = {})
        }
        if (index < results.lastIndex) {
            HorizontalDivider()
        }
    }
}

@Composable
private fun LineResultItem(line: Line, onLineClick: (Line) -> Unit) {
    ListItem(
        modifier = Modifier.clickable { onLineClick(line) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = { Text(line.description) },
        leadingContent = {
            LineIndicatorMedium(line)
        },
    )
}

@Composable
private fun StopResultItem(stop: Stop, lines: List<Line>, onStopClick: (Stop) -> Unit) {
    ListItem(
        modifier = Modifier.clickable { onStopClick(stop) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = { Text(stop.description) },
        overlineContent = { Text(stop.code.toString()) },
        supportingContent = { SupportingLines(lines) },
    )
}

@Composable
private fun SupportingLines(lines: List<Line>) {
    Row {
        lines.forEach { line ->
            LineIndicatorSmall(line = line)
            Spacer(Modifier.size(4.dp))
        }
    }
}

@Preview
@Composable
private fun Preview() {
    SevTheme {
        Column {
            SbSearchBar()
            Spacer(Modifier.size(32.dp))
            SbSearchBar(defaultExpanded = true, defaultText = "macar")

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
            StopResultItem(stop = Stubs.stops.random(), lines = Stubs.lines.shuffled().take(3), {})
        }
    }
}