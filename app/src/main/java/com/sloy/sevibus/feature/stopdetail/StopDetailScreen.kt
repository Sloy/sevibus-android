package com.sloy.sevibus.feature.stopdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.sloy.sevibus.R
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.BusArrival
import com.sloy.sevibus.domain.model.LineSummary
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.domain.model.description1
import com.sloy.sevibus.domain.model.description2
import com.sloy.sevibus.navigation.NavigationDestination
import com.sloy.sevibus.ui.components.ArrivalElement
import com.sloy.sevibus.ui.components.LineIndicatorMedium
import com.sloy.sevibus.ui.components.SevTopAppBar
import com.sloy.sevibus.ui.components.StopDetailInfoItem
import com.sloy.sevibus.ui.icons.SevIcons
import com.sloy.sevibus.ui.icons.Stop
import com.sloy.sevibus.ui.preview.ScreenPreview
import com.sloy.sevibus.ui.theme.AlexGreySurface
import com.sloy.sevibus.ui.theme.SevTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.stopDetailRoute() {
    composable<NavigationDestination.StopDetail> { stackEntry ->
        val destination = stackEntry.toRoute<NavigationDestination.StopDetail>()
        StopDetailScreen(destination.stopId, embedded = false)
    }
}

@Composable
fun StopDetailScreen(code: StopId, embedded: Boolean = false) {
    val viewModel = koinViewModel<StopDetailViewModel>(key = code.toString()) { parametersOf(code) }
    val state by viewModel.state.collectAsState(StopDetailScreenState())
    StopDetailScreen(state, embedded)
}

@Composable
fun StopDetailScreen(state: StopDetailScreenState, embedded: Boolean = false) {
    Column {
        val title = "Parada ${(state.stopState as? StopState.Loaded)?.stop?.code ?: ""}"
        if (!embedded) {
            SevTopAppBar(
                title = {
                    Text(
                        title, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        style = SevTheme.typography.headingLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null,
                            tint = SevTheme.colorScheme.onSurface,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            Icons.Default.FavoriteBorder, contentDescription = null,
                            tint = SevTheme.colorScheme.onSurface,
                        )
                    }
                }
            )
        }

        if (state.stopState is StopState.Loaded) {
            if (!embedded) {
                StopDetailInfoItem(state.stopState.stop, modifier = Modifier.padding(16.dp), showStopCode = false)
            }
            if (embedded) {
                Row(Modifier.padding(horizontal = 16.dp)) {
                    Icon(
                        SevIcons.Stop,
                        contentDescription = null,
                        tint = SevTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            state.stopState.stop.description1,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = SevTheme.typography.headingSmall
                        )
                        state.stopState.stop.description2?.let {
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
                            title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = SevTheme.typography.bodyExtraSmall,
                            color = SevTheme.colorScheme.onSurfaceVariant
                        )

                    }
                    IconButton(onClick = { /*TODO*/ }, modifier = Modifier.padding(start = 8.dp)) {
                        Icon(
                            Icons.Outlined.FavoriteBorder, contentDescription = stringResource(R.string.content_description_favorite_add),
                            tint = SevTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
            }
        }

        when (state.arrivalsState) {
            is ArrivalsState.Loaded -> BusArrivals(state.arrivalsState.arrivals)
            is ArrivalsState.Failed -> BusArrivalsFailure(state.arrivalsState.throwable)
            is ArrivalsState.Loading -> if (state.stopState is StopState.Loaded) {
                BusArrivalsLoading(lines = state.stopState.stop.lines)
            } else {
                BusArrivalsLoading()
            }
        }
    }
}

@Composable
private fun BusArrivals(arrivals: List<BusArrival>) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Líneas", style = SevTheme.typography.headingSmall, modifier = Modifier.padding(bottom = 4.dp))
        arrivals.forEach { arrival ->

            ListItem(
                colors = ListItemDefaults.colors(containerColor = AlexGreySurface),
                modifier = Modifier.clip(MaterialTheme.shapes.medium),
                headlineContent = { Text(text = arrival.route.destination) },
                leadingContent = { LineIndicatorMedium(line = arrival.line) },
                trailingContent = {
                    ArrivalElement(arrival)
                }
            )
        }
    }
}


@Composable
private fun BusArrivalsLoading() {
    Box(Modifier.fillMaxSize()) {
        CircularProgressIndicator(Modifier.align(Alignment.Center))
    }
}

@Composable
private fun BusArrivalsLoading(lines: List<LineSummary>) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Líneas", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 4.dp))
        lines.forEach { line ->
            ListItem(
                colors = ListItemDefaults.colors(containerColor = AlexGreySurface),
                modifier = Modifier.clip(MaterialTheme.shapes.medium),
                headlineContent = {
                    CircularProgressIndicator(Modifier.size(24.dp))
                },
                leadingContent = { LineIndicatorMedium(line) },
                trailingContent = {
                }
            )
        }
    }
}

@Composable
fun BusArrivalsFailure(throwable: Throwable) {
    Column(Modifier.padding(16.dp)) {
        Text("Ocurrió un error :(", style = MaterialTheme.typography.titleLarge)
        Text(throwable.localizedMessage?.toString() ?: throwable::class.qualifiedName ?: "Unknown error")
    }
}

@Preview
@Composable
private fun Preview() {
    ScreenPreview {
        StopDetailScreen(
            StopDetailScreenState(
                stopState = StopState.Loaded(Stubs.stops[1]),
                arrivalsState = ArrivalsState.Loaded(Stubs.arrivals)
            )
        )
    }
}

@Preview
@Composable
private fun EmbeddedPreview() {
    ScreenPreview {
        StopDetailScreen(
            StopDetailScreenState(
                stopState = StopState.Loaded(Stubs.stops[1]),
                arrivalsState = ArrivalsState.Loaded(Stubs.arrivals)
            ), embedded = true
        )
    }
}

@Preview
@Composable
private fun LoadingPreview() {
    ScreenPreview {
        StopDetailScreen(
            StopDetailScreenState(
                stopState = StopState.Loading,
                arrivalsState = ArrivalsState.Loading
            )
        )
    }
}

@Preview
@Composable
private fun LoadingWithStopPreview() {
    ScreenPreview {
        StopDetailScreen(
            StopDetailScreenState(
                stopState = StopState.Loaded(Stubs.stops[1]),
                arrivalsState = ArrivalsState.Loading
            )
        )
    }
}

@Preview
@Composable
private fun AllFailedPreview() {
    ScreenPreview {
        StopDetailScreen(
            StopDetailScreenState(
                stopState = StopState.Failed(IllegalStateException("Stop error")),
                arrivalsState = ArrivalsState.Failed(IllegalStateException("Arrival error"))
            )
        )
    }
}

@Preview
@Composable
private fun ArrivalFailedPreview() {
    ScreenPreview {
        StopDetailScreen(
            StopDetailScreenState(
                stopState = StopState.Loaded(Stubs.stops[1]),
                arrivalsState = ArrivalsState.Failed(IllegalStateException("Arrival error"))
            )
        )
    }
}


@Preview
@Composable
private fun StopFailedPreview() {
    ScreenPreview {
        StopDetailScreen(
            StopDetailScreenState(
                stopState = StopState.Failed(IllegalStateException("Stop error")),
                arrivalsState = ArrivalsState.Loaded(Stubs.arrivals)
            )
        )
    }
}

