package com.sloy.sevibus.feature.lines

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.Line
import com.sloy.sevibus.navigation.NavigationDestination
import com.sloy.sevibus.ui.components.LineElement
import com.sloy.sevibus.ui.preview.ScreenPreview
import com.sloy.sevibus.ui.theme.SevTheme
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.linesRoute(onNavigate: (NavigationDestination) -> Unit) {
    composable<NavigationDestination.Lines> {
        LinesScreen(onNavigate)
    }
}

@Composable
fun LinesScreen(onNavigate: (NavigationDestination) -> Unit, modifier: Modifier = Modifier) {
    if (!LocalView.current.isInEditMode) {

        val viewModel = koinViewModel<LinesViewModel>()
        val state by viewModel.state.collectAsState()
        LinesScreen(
            state,
            onLineClick = {
                onNavigate(NavigationDestination.LineStops(it.id))
            },
            modifier
        )
    } else {
        LinesScreen(state = LinesScreenState.Content(Stubs.groupsOfLines), {}, modifier)
    }
}

@Composable
private fun LinesScreen(state: LinesScreenState, onLineClick: (Line) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        when (state) {
            is LinesScreenState.Loading -> {
                CircularProgressIndicator(Modifier.size(48.dp))
            }

            is LinesScreenState.Error -> {
                Text("Error")
            }

            is LinesScreenState.Content -> {
                LazyColumn(Modifier.padding(horizontal = 16.dp)) {
                    item { Text("Líneas", style = SevTheme.typography.headingLarge, modifier = Modifier.padding(bottom = 24.dp)) }
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
        style = SevTheme.typography.headingSmall,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}


@Preview
@Composable
private fun LinesScreenPreview() {
    ScreenPreview {
        LinesScreen(state = LinesScreenState.Content(Stubs.groupsOfLines), {})
    }
}
