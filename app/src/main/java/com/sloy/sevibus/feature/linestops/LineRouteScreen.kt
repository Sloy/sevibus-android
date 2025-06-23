package com.sloy.sevibus.feature.linestops

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.sloy.sevibus.R
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.Line
import com.sloy.sevibus.domain.model.LineId
import com.sloy.sevibus.domain.model.Route
import com.sloy.sevibus.domain.model.RouteId
import com.sloy.sevibus.domain.model.Stop
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.feature.linestops.component.HighlightPosition
import com.sloy.sevibus.feature.linestops.component.ListPosition
import com.sloy.sevibus.feature.linestops.component.StopTimelineElement
import com.sloy.sevibus.ui.components.LineIndicator
import com.sloy.sevibus.ui.components.RouteTabsSelector
import com.sloy.sevibus.ui.preview.ScreenPreview
import com.sloy.sevibus.ui.theme.SevTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun LineRouteScreen(
    lineId: LineId,
    initialRouteId: RouteId?,
    highlightedStopId: StopId?,
    onStopClick: (Stop) -> Unit,
    onRouteSelected: (route: Route) -> Unit
) {
    val viewModel: LineRouteViewModel = koinViewModel(key = lineId.toString()) { parametersOf(lineId, initialRouteId) }
    val state by viewModel.state.collectAsState()
    LineRouteScreen(
        state,
        highlightedStopId,
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
    highlightedStopId: StopId?,
    onRouteSelected: (route: Route) -> Unit,
    onStopClick: (Stop) -> Unit,
) {
    Column {
        if (state is LineRouteScreenState.Content) {
            Row(Modifier.padding(horizontal = 16.dp)) {
                LineIndicator(state.line, Modifier.padding(end = 8.dp))
                Text(state.line.description, style = SevTheme.typography.headingSmall, maxLines = 1)
            }
            if (state.line.routes.size > 1) {
                RouteTabsSelector(
                    route1 = state.line.routes[0], route2 = state.line.routes[1], selected = state.selectedRoute.id, onRouteClicked = {
                        onRouteSelected(it)
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
        }

        when (state) {
            LineRouteScreenState.Error -> Text(stringResource(R.string.common_error))
            LineRouteScreenState.Loading -> CircularProgressIndicator()
            is LineRouteScreenState.Content.Full -> {
                RouteContent(state.stops, state.line, highlightedStopId, onStopClick)
            }

            else -> {}
        }
    }
}

@Composable
private fun RouteContent(
    stops: List<Stop>,
    line: Line,
    highlightedStopId: StopId?,
    onStopClick: (Stop) -> Unit
) {
    val state = remember { LazyListState() }
    LaunchedEffect(highlightedStopId) {
        if (highlightedStopId != null) {
            state.animateScrollToItem(stops.indexOfFirst { it.code == highlightedStopId }.takeIf { it != -1 } ?: 0)
        }
    }
    val highlightedStopIndex = stops.indexOfFirst { it.code == highlightedStopId }.takeIf { it != -1 }
    LazyColumn(state = state, contentPadding = PaddingValues(vertical = 16.dp)) {
        itemsIndexed(stops) { index, stop ->
            val highlightPosition = when {
                highlightedStopIndex == null -> HighlightPosition.None
                index == highlightedStopIndex -> HighlightPosition.Highlighted
                index > highlightedStopIndex -> HighlightPosition.AfterHighlighted
                else -> HighlightPosition.BeforeHighlighted
            }
            StopTimelineElement(
                stop,
                highlightPosition = highlightPosition,
                listPosition = when (index) {
                    0 -> ListPosition.Start
                    stops.lastIndex -> ListPosition.End
                    else -> ListPosition.Middle
                },
                color = line.color,
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
                line = Stubs.lines[0],
                stops = Stubs.stops.shuffled(),
                selectedRoute = Stubs.lines[0].routes.first(),
            ),
            highlightedStopId = null,
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
            highlightedStopId = null,
            onRouteSelected = {},
            onStopClick = {},
        )
    }
}
