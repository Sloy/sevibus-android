package com.sloy.sevibus.feature.linestops

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.Line
import com.sloy.sevibus.domain.model.LineId
import com.sloy.sevibus.domain.model.Route
import com.sloy.sevibus.domain.model.Stop
import com.sloy.sevibus.domain.model.primary
import com.sloy.sevibus.feature.linestops.component.ListPosition
import com.sloy.sevibus.feature.linestops.component.StopTimelineElement
import com.sloy.sevibus.navigation.NavigationDestination
import com.sloy.sevibus.ui.components.LineIndicatorMedium
import com.sloy.sevibus.ui.components.RouteTabsSelector
import com.sloy.sevibus.ui.preview.ScreenPreview
import com.sloy.sevibus.ui.theme.SevTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.lineStopsRoute(navController: NavController, onRouteSelected: (route: Route) -> Unit) {
    composable<NavigationDestination.LineStops> { stackEntry ->
        val destination = stackEntry.toRoute<NavigationDestination.LineStops>()
        LineRouteScreen(
            destination.lineId,
            onStopClick = { navController.navigate(NavigationDestination.StopDetail(it.code)) },
            onRouteSelected = onRouteSelected
        )
    }
}

@Composable
private fun LineRouteScreen(
    lineId: LineId,
    onStopClick: (Stop) -> Unit,
    onRouteSelected: (route: Route) -> Unit
) {
    val viewModel: LineRouteViewModel = koinViewModel { parametersOf(lineId) }
    val state by viewModel.state.collectAsState()
    LineRouteScreen(
        state,
        onRouteSelected = {
            viewModel.onRouteSelected(it)
            onRouteSelected(it)
        },
        onStopClick = onStopClick,
    )
}

@Composable
private fun LineRouteScreen(
    state: LineRouteScreenState,
    onRouteSelected: (route: Route) -> Unit,
    onStopClick: (Stop) -> Unit,
) {
    Column {
        if (state is LineRouteScreenState.Content) {
            Row(Modifier.padding(horizontal = 16.dp)) {
                LineIndicatorMedium(state.line, Modifier.padding(end = 8.dp))
                Text(state.line.description, style = SevTheme.typography.headingSmall, maxLines = 1)
            }
            if (state.line.routes.size > 1) {
                RouteTabsSelector(
                    route1 = state.line.routes[0], route2 = state.line.routes[1], selected = state.selectedRoute.id, onRouteClicked = {
                        onRouteSelected(it)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp).padding(top = 16.dp)
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 16.dp))
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
    LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
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
private fun PreviewWithRoutes() {
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

@Preview
@Composable
private fun PreviewWithoutRoutes() {
    ScreenPreview {
        LineRouteScreen(
            state = LineRouteScreenState.Content.Full(
                line = Stubs.lines.first { it.routes.size == 1 },
                stops = Stubs.stops.shuffled(),
                selectedRoute = Stubs.lines[2].routes.first(),
            ),
            onRouteSelected = {},
            onStopClick = {},
        )
    }
}
