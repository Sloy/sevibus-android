package com.sloy.sevibus.feature.foryou.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.BusArrival
import com.sloy.sevibus.domain.model.FavoriteStop
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.domain.model.toImageVector
import com.sloy.sevibus.ui.components.ArrivalTimeElement
import com.sloy.sevibus.ui.components.ArrivalTimeElementShimmer
import com.sloy.sevibus.ui.formatter.formatSubtitle
import com.sloy.sevibus.ui.formatter.formatTitle
import com.sloy.sevibus.ui.shimmer.shimmerLoadingAnimation
import com.sloy.sevibus.ui.theme.SevTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FavoriteListItem(
    favoriteStop: FavoriteStop,
    onStopClicked: (code: StopId) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!LocalView.current.isInEditMode) {
        val viewModel = koinViewModel<FavoriteItemViewModel>(key = favoriteStop.viewModelKey()) { parametersOf(favoriteStop) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        FavoriteListItem(state, onStopClicked, modifier)
    } else {
        FavoriteListItem(
            FavoriteItemState.Loaded(Stubs.favorites.first(), Stubs.arrivals),
            onStopClicked = onStopClicked,
            modifier = modifier,
        )
    }
}

private fun FavoriteStop.viewModelKey(): String {
    val selectedLinesKey = selectedLineIds
        ?.map { it.toString() }
        ?.sorted()
        ?.joinToString(separator = ", ", prefix = "[", postfix = "]")
        ?: "null"

    return listOf(stop.code.toString(), customName, customIcon, selectedLinesKey)
        .joinToString(separator = "|")
}
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FavoriteListItem(
    state: FavoriteItemState,
    onStopClicked: (code: StopId) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is FavoriteItemState.Loading -> {
            FavoriteListItem(
                state.favorite,
                arrivals = null,
                onStopClicked = onStopClicked,
                modifier = modifier,
            )
        }

        is FavoriteItemState.Loaded -> {
            FavoriteListItem(
                state.favorite,
                arrivals = state.arrivals,
                onStopClicked = onStopClicked,
                modifier = modifier,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FavoriteListItem(
    favorite: FavoriteStop,
    arrivals: List<BusArrival>?,
    onStopClicked: (code: StopId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = { onStopClicked(favorite.stop.code) },
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp)) {
            Icon(favorite.customIcon.toImageVector(), contentDescription = null, tint = SevTheme.colorScheme.primary)
            Column(Modifier.padding(start = 16.dp)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        favorite.formatTitle(), maxLines = 1, overflow = TextOverflow.Ellipsis, style = SevTheme.typography.bodyStandardBold
                    )
                    Text(
                        " • ${favorite.stop.code}",
                        maxLines = 1,
                        style = SevTheme.typography.bodySmall,
                        color = SevTheme.colorScheme.onSurfaceVariant
                    )
                }
                favorite.formatSubtitle()?.let { subtitle ->
                    Text(
                        subtitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = SevTheme.typography.bodySmall,
                        color = SevTheme.colorScheme.onSurfaceVariant
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    if (arrivals != null) {
                        arrivals.forEach {
                            ArrivalTimeElement(it, showLine = true)
                        }
                    } else {
                        val linesToShow = when {
                            favorite.selectedLineIds == null -> favorite.stop.lines
                            favorite.selectedLineIds.isEmpty() -> emptyList()
                            else -> favorite.stop.lines.filter { line ->
                                favorite.selectedLineIds.contains(line.id)
                            }
                        }
                        linesToShow.forEach { line ->
                            ArrivalTimeElementShimmer(line)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteListItemShimmer(modifier: Modifier = Modifier) {
    Row(modifier.padding(vertical = 8.dp, horizontal = 32.dp)) {
        Box(
            Modifier
                .size(24.dp)
                .shimmerLoadingAnimation()
        )
        Column(Modifier.padding(start = 16.dp)) {
            Box(
                Modifier
                    .size(80.dp, 14.dp)
                    .shimmerLoadingAnimation()
            )
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .size(200.dp, 12.dp)
                    .shimmerLoadingAnimation()
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    Modifier
                        .size(32.dp, 16.dp)
                        .shimmerLoadingAnimation()
                )
                Box(
                    Modifier
                        .size(32.dp, 16.dp)
                        .shimmerLoadingAnimation()
                )
                Box(
                    Modifier
                        .size(32.dp, 16.dp)
                        .shimmerLoadingAnimation()
                )
            }
        }
    }
}

@Preview
@Composable
private fun LoadedPreview() {
    SevTheme {
        Surface {
            Column(Modifier.padding(16.dp)) {
                Stubs.favorites.forEach {
                    FavoriteListItem(
                        favorite = it,
                        onStopClicked = {},
                        arrivals = Stubs.arrivals.shuffled().take(5).sorted(),
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun LoadingArrivalsPreview() {
    SevTheme {
        Surface {
            Column(Modifier.padding(16.dp)) {
                Stubs.favorites.forEach {
                    FavoriteListItem(
                        favorite = it,
                        onStopClicked = {},
                        arrivals = null,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 400)
@Composable
private fun LoadingPreview() {
    SevTheme {
        Surface {
            Column {
                FavoriteListItemShimmer()
                Spacer(Modifier.size(4.dp))
                FavoriteListItemShimmer()
                Spacer(Modifier.size(4.dp))
                FavoriteListItemShimmer()
                Spacer(Modifier.size(4.dp))
            }
        }
    }
}
