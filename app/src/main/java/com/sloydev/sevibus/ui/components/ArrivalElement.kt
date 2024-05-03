package com.sloydev.sevibus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.domain.model.BusArrival
import com.sloydev.sevibus.ui.theme.SevTheme


@Composable
fun ArrivalElement(arrival: BusArrival, showLine: Boolean = false) {
    Box(
        Modifier
            .clip(SevTheme.shapes.small)
            .background(SevTheme.colorScheme.surfaceContainerHighest)
    ) {
        if (showLine) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LineIndicatorMedium(arrival.line, modifier = Modifier.padding(4.dp))
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

fun BusArrival.toText() = when (minutes) {
    null -> "+30 min"
    0 -> "Llegando..."
    else -> "$minutes min"
}

@Preview
@Composable
private fun Preview() {
    SevTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .background(SevTheme.colorScheme.surface)
                .padding(32.dp)
        ) {
            Stubs.arrivals.forEach {
                ArrivalElement(it, showLine = false)
            }

            Stubs.arrivals.forEach {
                ArrivalElement(it, showLine = true)
            }
        }
    }
}
