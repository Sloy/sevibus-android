package com.sloy.sevibus.feature.foryou.nearby

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.sloy.sevibus.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.BusArrival
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.ui.components.ArrivalTimeElement
import com.sloy.sevibus.ui.components.ArrivalTimeElementShimmer
import com.sloy.sevibus.ui.formatter.formatSubtitle
import com.sloy.sevibus.ui.formatter.formatTitle
import com.sloy.sevibus.ui.icons.SevIcons
import com.sloy.sevibus.ui.icons.Stop
import com.sloy.sevibus.ui.shimmer.shimmerLoadingAnimation
import com.sloy.sevibus.ui.theme.SevTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NearbyListItem(
    nearbyStop: NearbyStop,
    onStopClicked: (code: StopId) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!LocalView.current.isInEditMode) {
        val viewModel = koinViewModel<NearbyItemViewModel>(key = nearbyStop.stop.code.toString()) { parametersOf(nearbyStop) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        NearbyListItem(state, onStopClicked, modifier)
    } else {
        NearbyListItem(
            NearbyItemState.Loaded(Stubs.nearby.first(), Stubs.arrivals),
            onStopClicked = onStopClicked,
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NearbyListItem(
    state: NearbyItemState,
    onStopClicked: (code: StopId) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is NearbyItemState.Loading -> {
            NearbyListItem(
                state.nearby,
                arrivals = null,
                onStopClicked = onStopClicked,
                modifier = modifier,
            )
        }

        is NearbyItemState.Loaded -> {
            NearbyListItem(
                state.nearby,
                arrivals = state.arrivals,
                onStopClicked = onStopClicked,
                modifier = modifier,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NearbyListItem(
    nearby: NearbyStop,
    arrivals: List<BusArrival>?,
    onStopClicked: (code: StopId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = { onStopClicked(nearby.stop.code) },
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(Modifier.padding(vertical = 16.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    SevIcons.Stop,
                    contentDescription = null,
                    tint = SevTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    stringResource(R.string.common_distance_meters, nearby.distance),
                    style = SevTheme.typography.bodyExtraSmall,
                    color = SevTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Column(Modifier.padding()) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        nearby.stop.formatTitle(null),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = SevTheme.typography.bodyStandardBold
                    )
                    Text(
                        " • ${nearby.stop.code}",
                        maxLines = 1,
                        style = SevTheme.typography.bodySmall,
                        color = SevTheme.colorScheme.onSurfaceVariant
                    )
                }
                nearby.stop.formatSubtitle(null)?.let { subtitle ->
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
                        nearby.stop.lines.forEach { line ->
                            ArrivalTimeElementShimmer(line)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NearbyListItemShimmer(modifier: Modifier = Modifier) {
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
        Column(Modifier.padding(16.dp)) {
            Stubs.nearby.forEach {
                NearbyListItem(
                    nearby = it,
                    onStopClicked = {},
                    arrivals = Stubs.arrivals.shuffled().take(5).sorted(),
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun LoadingArrivalsPreview() {
    SevTheme {
        Column(Modifier.padding(16.dp)) {
            Stubs.nearby.forEach {
                NearbyListItem(
                    nearby = it,
                    onStopClicked = {},
                    arrivals = null,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
        }
    }
}

@Preview(widthDp = 400)
@Composable
private fun LoadingPreview() {
    SevTheme {
        Column {
            NearbyListItemShimmer()
            Spacer(Modifier.size(4.dp))
            NearbyListItemShimmer()
            Spacer(Modifier.size(4.dp))
            NearbyListItemShimmer()
            Spacer(Modifier.size(4.dp))
        }
    }
}
