package com.sloy.sevibus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.sloy.sevibus.R
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.BusArrival
import com.sloy.sevibus.domain.model.LineSummary
import com.sloy.sevibus.domain.model.toSummary
import com.sloy.sevibus.ui.shimmer.Shimmer
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
fun BusArrivalListItemLoading() {
    Surface(Modifier.clip(MaterialTheme.shapes.medium)) {
        Row(
            Modifier
                .background(SevTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Shimmer(Modifier.size(24.dp, 24.dp))

            Spacer(Modifier.width(16.dp))

            Shimmer(Modifier.size(width = 120.dp, height = 20.dp))

            Spacer(Modifier.weight(1f))

            ArrivalTimeElementShimmer(line = null)
        }
    }
}

@Composable
fun BusArrivalListItemLoading(line: LineSummary) {
    Surface(Modifier.clip(MaterialTheme.shapes.medium)) {
        Row(
            Modifier
                .background(SevTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            LineIndicator(line = line)

            Spacer(Modifier.width(16.dp))

            Shimmer(Modifier.size(width = 120.dp, height = 20.dp))

            Spacer(Modifier.weight(1f))

            ArrivalTimeElementShimmer(line = null)
        }
    }
}

@Composable
fun BusArrivalListItem(arrival: BusArrival, isHighlighted: Boolean, onClick: (BusArrival) -> Unit) {
    Surface(
        onClick = { onClick(arrival) },
        Modifier.clip(MaterialTheme.shapes.medium)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(if (isHighlighted) SevTheme.colorScheme.surfaceVariant else SevTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            LineIndicator(line = arrival.line)
            Spacer(Modifier.width(16.dp))
            if ((arrival as? BusArrival.Available)?.isLastBus == true) {
                Column {
                    Text(text = arrival.route.destination)
                    Text(text = stringResource(R.string.arrivals_last_bus), style = SevTheme.typography.bodyExtraSmall, color = SevTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Text(text = arrival.route.destination)
            }
            Spacer(Modifier.weight(1f))
            ArrivalTimeElement(arrival)
        }
    }
}


@Preview(showBackground = true)
@Composable
internal fun BusArrivalAvailablePreview() {
    SevTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
        ) {
            BusArrivalListItem(Stubs.arrivals[0], isHighlighted = false, {})
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun BusArrivalLastBusPreview() {
    SevTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
        ) {
            BusArrivalListItem(
                BusArrival.Available.Arriving(
                    bus = 1,
                    distance = 50,
                    line = Stubs.lines.random().toSummary(),
                    route = Stubs.routes.random(),
                    isLastBus = true
                ), isHighlighted = false, {})
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun BusArrivalHighlightedPreview() {
    SevTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
        ) {
            BusArrivalListItem(Stubs.arrivals[0], isHighlighted = true, {})
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun BusArrivalNotAvailablePreview() {
    SevTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
        ) {
            BusArrivalListItem(BusArrival.NotAvailable(Stubs.lines.random().toSummary(), Stubs.routes.random()), isHighlighted = false, {})
        }
    }
}


@Preview(showBackground = true)
@Composable
internal fun BusArrivalLoadingWithLinePreview() {
    SevTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
        ) {
            BusArrivalListItemLoading(Stubs.lines[0].toSummary())
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun BusArrivalLoadingPreview() {
    SevTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
        ) {
            BusArrivalListItemLoading()
        }
    }
}
