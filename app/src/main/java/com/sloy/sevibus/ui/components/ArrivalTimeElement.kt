package com.sloy.sevibus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.BusArrival
import com.sloy.sevibus.domain.model.LineSummary
import com.sloy.sevibus.domain.model.toSummary
import com.sloy.sevibus.ui.shimmer.shimmerLoadingAnimation
import com.sloy.sevibus.ui.theme.SevTheme


@Composable
fun ArrivalTimeElement(arrival: BusArrival, showLine: Boolean = false) {
    Box(
        Modifier
            .clip(SevTheme.shapes.small)
            .background(SevTheme.colorScheme.outlineVariant)
    ) {
        if (showLine) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LineIndicator(arrival.line, modifier = Modifier.padding(4.dp))
                Text(
                    arrival.toText(),
                    style = SevTheme.typography.bodySmallBold,
                    color = SevTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 2.dp, end = 6.dp)
                )
            }
        } else {
            Text(
                arrival.toText(),
                style = SevTheme.typography.bodySmallBold,
                color = SevTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun ArrivalTimeElementShimmer(line: LineSummary?, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(SevTheme.shapes.small)
            .background(SevTheme.colorScheme.outlineVariant)
            .size(
                width = if (line != null) 82.dp else 52.dp,
                height = if (line != null) 30.dp else 24.dp
            )
            .shimmerLoadingAnimation()
    ) {
        if (line != null) {
            LineIndicator(line, modifier = Modifier.padding(4.dp))
        }
    }
}

private fun BusArrival.toText() = when (this) {
    is BusArrival.Available.Arriving -> "Llegando..."
    is BusArrival.Available.Estimation -> "$minutes min"
    is BusArrival.Available.NoEstimation -> "+30 min"
    is BusArrival.NotAvailable -> "No disponible"
}

@Preview(showBackground = true)
@Composable
internal fun ArrivalTimeElementPreview() {
    SevTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .background(SevTheme.colorScheme.surface)
                .padding(32.dp)
        ) {
            Stubs.arrivals.forEach {
                ArrivalTimeElement(it, showLine = false)
            }

            ArrivalTimeElementShimmer(null)
            ArrivalTimeElementShimmer(Stubs.lines.first().toSummary())

            Stubs.arrivals.forEach {
                ArrivalTimeElement(it, showLine = true)
            }
        }
    }
}
