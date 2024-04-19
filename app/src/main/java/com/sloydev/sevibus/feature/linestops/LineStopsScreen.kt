package com.sloydev.sevibus.feature.linestops

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.feature.stopdetail.navigateToStopDetail
import com.sloydev.sevibus.navigation.TopLevelDestination
import com.sloydev.sevibus.ui.ScreenPreview
import com.sloydev.sevibus.ui.components.LineIndicatorSmall

fun NavGraphBuilder.lineStopsRoute(navController: NavController) {
    composable(TopLevelDestination.LINES.route + "{line}/stops") { stackEntry ->
        val line = stackEntry.arguments!!.getString("line")
        StopsScreen(
            LineStopsScreenState(
                line = Stubs.lines.first { it.label == line },
                directions = listOf("HOSPITAL V.ROCIO", "POLIGONO NORTE"),
                stops = Stubs.stops,
            ),
            onStopClick = { navController.navigateToStopDetail(it.code) }
        )
    }
}

fun NavController.navigateToLineStops(line: String) {
    navigate(TopLevelDestination.LINES.route + "$line/stops")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopsScreen(state: LineStopsScreenState, onStopClick: (Stop) -> Unit) {
    Column {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LineIndicatorSmall(state.line, Modifier.padding(end = 8.dp))
                    Text(state.line.description, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            },
            navigationIcon = {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        )
        var tab by rememberSaveable { mutableStateOf(0) }
        TabRow(selectedTabIndex = tab) {
            Tab(
                selected = tab == 0,
                onClick = { tab = 0 },
                text = { Text(text = state.directions[0], maxLines = 2, overflow = TextOverflow.Ellipsis) }
            )
            Tab(
                selected = tab == 1,
                onClick = { tab = 1 },
                text = { Text(text = state.directions[1], maxLines = 2, overflow = TextOverflow.Ellipsis) }
            )
        }

        LazyColumn {
            itemsIndexed(state.stops) { index, stop ->
                StopListItem(
                    stop,
                    lines = remember(stop) { Stubs.lines.take(listOf(1, 1, 1, 1, 2, 2, 2, 3, 3, 4).random()) },
                    listPosition = when (index) {
                        0 -> ListPosition.Start
                        state.stops.lastIndex -> ListPosition.End
                        else -> ListPosition.Middle
                    },
                    color = Color(state.line.colorHex),
                    onStopClick = onStopClick,
                )
            }
        }
    }
}

@Preview
@Composable
private fun StopsScreenPreview() {
    ScreenPreview {
        StopsScreen(
            LineStopsScreenState(
                line = Stubs.lines[2],
                directions = listOf("HOSPITAL V.ROCIO", "POLIGONO NORTE"),
                stops = Stubs.stops,
            ),
            onStopClick = {}
        )
    }
}