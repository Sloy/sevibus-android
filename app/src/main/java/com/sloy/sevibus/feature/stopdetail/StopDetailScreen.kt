package com.sloy.sevibus.feature.stopdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sloy.sevibus.R
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.BusArrival
import com.sloy.sevibus.domain.model.CustomIcon
import com.sloy.sevibus.domain.model.FavoriteStop
import com.sloy.sevibus.domain.model.LineId
import com.sloy.sevibus.domain.model.Stop
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.domain.model.description1
import com.sloy.sevibus.domain.model.description2
import com.sloy.sevibus.domain.model.toImageVector
import com.sloy.sevibus.domain.model.toSummary
import com.sloy.sevibus.infrastructure.BuildVariant
import com.sloy.sevibus.infrastructure.EventCollector
import com.sloy.sevibus.ui.components.BusArrivalListItem
import com.sloy.sevibus.ui.components.BusArrivalListItemLoading
import com.sloy.sevibus.ui.components.CircularIconButton
import com.sloy.sevibus.ui.components.InfoBannerComponent
import com.sloy.sevibus.ui.components.LineIndicator
import com.sloy.sevibus.ui.formatter.formatSubtitle
import com.sloy.sevibus.ui.formatter.formatTitle
import com.sloy.sevibus.ui.icons.SevIcons
import com.sloy.sevibus.ui.icons.Stop
import com.sloy.sevibus.ui.preview.ScreenPreview
import com.sloy.sevibus.ui.snackbar.LocalSnackbarHostState
import com.sloy.sevibus.ui.theme.SevTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun StopDetailScreen(code: StopId, highlighedLine: LineId?, onArrivalClick: (BusArrival, StopId) -> Unit) {
    val viewModel = koinViewModel<StopDetailViewModel>(key = code.toString()) { parametersOf(code) }
    val snackbarHost = LocalSnackbarHostState.current
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val loginRequiredMessage = stringResource(R.string.stopdetail_login_required)
    val loginButtonText = stringResource(R.string.common_login)
    EventCollector(viewModel.events) { event ->
        when (event) {
            is StopDetailScreenEvent.LoginRequired -> {
                if (snackbarHost.showSnackbar(
                        loginRequiredMessage,
                        loginButtonText,
                        duration = SnackbarDuration.Long
                    ) == SnackbarResult.ActionPerformed
                ) {
                    event.loginAction(context)
                        .onSuccess { viewModel.onFavoriteClick() }
                }
            }
        }
    }
    StopDetailScreen(
        state,
        highlighedLine,
        onArrivalClick = { onArrivalClick(it, code) },
        onFavoriteClick = { viewModel.onFavoriteClick() })
}

@Composable
fun StopDetailScreen(
    state: StopDetailScreenState,
    highlighedLine: LineId?,
    onArrivalClick: (BusArrival) -> Unit,
    onFavoriteClick: () -> Unit
) {
    Column {

        if (state is StopDetailScreenState.Loaded) {
            val title = stringResource(R.string.common_stop_with_code, state.stop.code)
            StopDetailsHeader(state, title, onFavoriteClick)
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
        }
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            when (state) {
                is StopDetailScreenState.Loaded -> {
                    Text(stringResource(R.string.stopdetail_lines_section), style = SevTheme.typography.headingSmall, modifier = Modifier.padding(bottom = 4.dp))
                    when (state.arrivalsState) {
                        is ArrivalsState.Loaded -> {
                            state.arrivalsState.arrivals.forEach {
                                BusArrivalListItem(
                                    it,
                                    isHighlighted = it.line.id == highlighedLine,
                                    onClick = { onArrivalClick(it) })
                            }
                        }

                        is ArrivalsState.Loading -> state.stop.lines.forEach { line -> BusArrivalListItemLoading(line) }

                        is ArrivalsState.Failed -> {
                            ArrivalsFailureBanner(state.arrivalsState.throwable)
                            Spacer(Modifier.height(16.dp))
                            state.arrivalsState.failedArrivals.forEach {
                                BusArrivalListItem(it, isHighlighted = false,
                                    onClick = { onArrivalClick(it) })
                            }
                        }
                    }
                }

                is StopDetailScreenState.Loading -> {
                    Spacer(Modifier.height(120.dp))
                    repeat(3) { BusArrivalListItemLoading() }
                }

                is StopDetailScreenState.Failed -> {

                    ArrivalsFailureBanner(state.throwable)
                }
            }
        }

    }
}

@Composable
private fun StopDetailsHeader(stopState: StopDetailScreenState.Loaded, title: String, onFavoriteClick: () -> Unit) {
    Row(Modifier.padding(horizontal = 16.dp)) {
        Icon(
            stopState.favorite?.customIcon?.toImageVector() ?: SevIcons.Stop,
            contentDescription = null,
            tint = SevTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 8.dp)
        )
        Column(Modifier.weight(1f)) {
            Text(
                stopState.stop.formatTitle(stopState.favorite),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = SevTheme.typography.headingSmall
            )
            stopState.stop.formatSubtitle(stopState.favorite)?.let { subtitle ->
                Text(
                    subtitle,
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
        Column(horizontalAlignment = Alignment.End) {
            CircularIconButton(onClick = onFavoriteClick, modifier = Modifier.padding(start = 8.dp)) {
                val (icon, color) = if (stopState.favorite != null) {
                    Icons.Outlined.Favorite to SevTheme.colorScheme.primary
                } else {
                    Icons.Outlined.FavoriteBorder to SevTheme.colorScheme.onSurfaceVariant
                }
                Icon(
                    icon,
                    contentDescription = stringResource(R.string.cd_favorite_add),
                    tint = color,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 16.dp)){
                stopState.stop.lines.forEach { LineIndicator(it) }
            }

        }
    }
}


@Composable
private fun BusArrivalsLoadingWithStop(stop: Stop) {
    stop.lines.forEach { line ->
        ListItem(
            colors = ListItemDefaults.colors(containerColor = SevTheme.colorScheme.surface),
            modifier = Modifier.clip(MaterialTheme.shapes.medium),
            headlineContent = {
                CircularProgressIndicator(Modifier.size(24.dp))
            },
            leadingContent = { LineIndicator(line) },
            trailingContent = {
            }
        )
    }
}

@Composable
fun ArrivalsFailureBanner(throwable: Throwable) {
    InfoBannerComponent(
        text = stringResource(R.string.stopdetail_error_loading_arrivals),
        icon = Icons.Filled.CloudOff,
    )
    val view = LocalView.current
    if (BuildVariant.isDebug() && !view.isInEditMode) {
        Text(throwable.localizedMessage?.toString() ?: throwable::class.qualifiedName ?: "Unknown error")
    }
}

@Preview
@Composable
private fun LoadedArrivalsPreview() {
    ScreenPreview {
        val arrivals = Stubs.arrivals
        StopDetailScreen(
            StopDetailScreenState.Loaded(
                Stubs.stops[1],
                favorite = FavoriteStop(Stubs.stops[1], "Casa", CustomIcon.Home),
                arrivalsState = ArrivalsState.Loaded(arrivals)
            ),
            highlighedLine = arrivals[2].line.id,
            {},
            {},
        )
    }
}

@Preview
@Composable
private fun LoadingArrivalsPreview() {
    ScreenPreview {
        StopDetailScreen(
            StopDetailScreenState.Loaded(Stubs.stops[1], arrivalsState = ArrivalsState.Loading(Stubs.lines.take(3).map { it.toSummary() })),
            highlighedLine = null,
            {},
            {},
        )
    }
}

@PreviewLightDark
@Composable
private fun FailedArrivalsPreview() {
    ScreenPreview {
        val failedArrivals = Stubs.lines.shuffled().take(3).map { BusArrival.NotAvailable(it.toSummary(), it.routes.first()) }
        StopDetailScreen(
            StopDetailScreenState.Loaded(
                Stubs.stops[1],
                arrivalsState = ArrivalsState.Failed(failedArrivals, IllegalStateException("Some preview error message"))
            ),
            highlighedLine = null,
            {},
            {},
        )
    }
}

@Preview
@Composable
private fun LoadingStopPreview() {
    ScreenPreview {
        StopDetailScreen(
            StopDetailScreenState.Loading,
            highlighedLine = null,
            {},
            {},
        )
    }
}

@Preview
@Composable
private fun FailedStopPreview() {
    ScreenPreview {
        StopDetailScreen(
            StopDetailScreenState.Failed(IllegalStateException("Stop error")),
            highlighedLine = null,
            {},
            {},
        )
    }
}
