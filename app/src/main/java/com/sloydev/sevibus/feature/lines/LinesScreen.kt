package com.sloydev.sevibus.feature.lines

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloydev.sevibus.R
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.ui.ScreenPreview
import com.sloydev.sevibus.ui.components.LineIndicatorMedium

@Composable
fun LinesRoute(onLineClick: (Line) -> Unit) {
    //TODO inject viewmodel and subscribe to state
    LinesScreen(LinesState.Content(Stubs.lines), onLineClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LinesScreen(state: LinesState, onLineClick: (Line) -> Unit = {}) {
    when (state) {
        is LinesState.Loading -> {
            CircularProgressIndicator()
        }

        is LinesState.Content -> {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text(text = stringResource(id = R.string.navigation_lines)) },
                )
                LazyColumn {
                    Stubs.lineTypes.forEach { lineType ->
                        val linesOfType = state.lines.filter { it.type == lineType }
                        if (linesOfType.isNotEmpty()) {
                            item { LineTypeTitle(lineType) }
                            items(linesOfType) { line ->
                                LineItem(line, onLineClick)
                            }
                            item { Spacer(Modifier.size(16.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LineTypeTitle(lineType: String) {
    Text(
        lineType,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(start = (16 + 8).dp)
    )
}

@Composable
private fun LineItem(it: Line, onLineClick: (Line) -> Unit) {
    Card(
        onClick = { onLineClick(it) },
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
    ) {
        ListItem(
            tonalElevation = 8.dp,
            headlineContent = { Text(it.description) },
            leadingContent = {
                LineIndicatorMedium(it)
            },
        )
    }
}


@Preview
@Composable
private fun LinesScreenPreview() {
    ScreenPreview {
        LinesScreen(state = LinesState.Content(Stubs.lines))
    }
}