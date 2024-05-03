package com.sloydev.sevibus.feature.linestops

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.domain.model.Line
import com.sloydev.sevibus.domain.model.LineId
import com.sloydev.sevibus.domain.model.Route
import com.sloydev.sevibus.domain.model.Stop
import com.sloydev.sevibus.domain.model.primary
import com.sloydev.sevibus.feature.linestops.component.ListPosition
import com.sloydev.sevibus.feature.linestops.component.StopTimelineElement
import com.sloydev.sevibus.feature.stopdetail.navigateToStopDetail
import com.sloydev.sevibus.navigation.TopLevelDestination
import com.sloydev.sevibus.ui.components.LineIndicatorSmall
import com.sloydev.sevibus.ui.components.RouteTabsSelector
import com.sloydev.sevibus.ui.components.SevTopAppBar
import com.sloydev.sevibus.ui.preview.ScreenPreview
import com.sloydev.sevibus.ui.theme.SevTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.lineStopsRoute(navController: NavController) {
    composable(TopLevelDestination.LINES.route + "{line}/stops") { stackEntry ->
        val lineId: LineId = stackEntry.arguments!!.getString("line")!!.toInt()
        LineRouteScreen(lineId, onStopClick = { navController.navigateToStopDetail(it.code) })
    }
}

fun NavController.navigateToLineStops(line: LineId) {
    navigate(TopLevelDestination.LINES.route + "$line/stops")
}

@Composable
private fun LineRouteScreen(lineId: LineId, onStopClick: (Stop) -> Unit) {
    val viewModel: LineRouteViewModel = koinViewModel { parametersOf(lineId) }
    val state by viewModel.state.collectAsState()
    LineRouteScreen(
        state,
        onRouteSelected = viewModel::onRouteSelected,
        onStopClick = onStopClick,
        embedded = false,
    )
}

@Composable
fun LineRouteScreen(
    state: LineRouteScreenState,
    onRouteSelected: (route: Route) -> Unit,
    onStopClick: (Stop) -> Unit,
    embedded: Boolean = false,
) {
    Column {
        if (!embedded) {
            SevTopAppBar(title = {
                if (state is LineRouteScreenState.Content) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LineIndicatorSmall(state.line, Modifier.padding(end = 8.dp))
                        Text(state.line.description, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            },
                navigationIcon = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null,
                            tint = SevTheme.colorScheme.onSurface,
                        )
                    }
                }
            )
        }
        if (state is LineRouteScreenState.Content) {
            if (state.line.routes.size > 1) {
                RouteTabsSelector(
                    route1 = state.line.routes[0], route2 = state.line.routes[1], selected = state.selectedRoute.id, onRouteClicked = {
                        onRouteSelected(it)
                    },
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        when (state) {
            LineRouteScreenState.Error -> Text("Error")
            LineRouteScreenState.Loading -> CircularProgressIndicator()
            is LineRouteScreenState.Content.Full -> {
                RouteContent(state.stops, state.line, onStopClick)
            }

            else -> {}
        }
    }
}

@Composable
private fun RouteContent(
    stops: List<Stop>,
    line: Line,
    onStopClick: (Stop) -> Unit
) {
    LazyColumn {
        itemsIndexed(stops) { index, stop ->
            StopTimelineElement(
                stop,
                listPosition = when (index) {
                    0 -> ListPosition.Start
                    stops.lastIndex -> ListPosition.End
                    else -> ListPosition.Middle
                },
                color = line.color.primary(),
                onStopClick = onStopClick,
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    ScreenPreview {
        LineRouteScreen(
            state = LineRouteScreenState.Content.Full(
                line = Stubs.lines[2],
                stops = Stubs.stops.shuffled(),
                selectedRoute = Stubs.lines[2].routes.first(),
            ),
            onRouteSelected = {},
            onStopClick = {},
        )
    }
}
