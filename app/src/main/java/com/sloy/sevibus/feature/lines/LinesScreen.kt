package com.sloy.sevibus.feature.lines

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sloy.sevibus.R
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.Line
import com.sloy.sevibus.domain.model.SearchResult
import com.sloy.sevibus.feature.linestops.navigateToLineStops
import com.sloy.sevibus.feature.search.SearchWidget
import com.sloy.sevibus.feature.stopdetail.navigateToStopDetail
import com.sloy.sevibus.navigation.TopLevelDestination
import com.sloy.sevibus.ui.components.LineElement
import com.sloy.sevibus.ui.components.SevCenterAlignedTopAppBar
import com.sloy.sevibus.ui.preview.ScreenPreview
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.linesRoute(navController: NavController) {
    composable(TopLevelDestination.LINES.route) {
        val viewModel = koinViewModel<LinesViewModel>()
        val state by viewModel.state.collectAsState()
        LinesScreen(state,
            onLineClick = { navController.navigateToLineStops(it.id) },
            onSearchResultClicked = {
                when (it) {
                    is SearchResult.LineResult -> navController.navigateToLineStops(it.line.id)
                    is SearchResult.StopResult -> navController.navigateToStopDetail(it.stop.code)
                }
            }
        )
    }
}

@Composable
private fun LinesScreen(state: LinesScreenState, onLineClick: (Line) -> Unit, onSearchResultClicked: (SearchResult) -> Unit) {
    Column {

        SevCenterAlignedTopAppBar(
            title = { Text(text = stringResource(id = R.string.navigation_lines)) },
        )

        when (state) {
            is LinesScreenState.Loading -> {
                CircularProgressIndicator(Modifier.size(48.dp))
            }

            is LinesScreenState.Error -> {
                Text("Error")
            }

            is LinesScreenState.Content -> {
                LazyColumn(Modifier.padding(horizontal = 16.dp)) {
                    item { SearchWidget(onSearchResultClicked = onSearchResultClicked) }
                    item { Spacer(Modifier.size(32.dp)) }
                    state.lineGroups.forEach { (group, lines) ->
                        if (lines.isNotEmpty()) {
                            item { LineGroupTitle(group) }
                            item { LinesCard(lines, onLineClick) }
                            item { Spacer(Modifier.size(32.dp)) }
                        }

                    }
                }
            }

        }
    }
}


@Composable
fun LinesCard(lines: List<Line>, onLineClick: (Line) -> Unit) {
    Card {
        Column {
            lines.forEachIndexed { index, line ->
                LineElement(line, onLineClick)
                if (index < lines.lastIndex) {
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
private fun LineGroupTitle(lineGroup: String) {
    Text(
        lineGroup,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(start = (16).dp, bottom = 8.dp)
    )
}


@Preview
@Composable
private fun LinesScreenPreview() {
    ScreenPreview {
        LinesScreen(state = LinesScreenState.Content(Stubs.groupsOfLines), {}, {})
    }
}