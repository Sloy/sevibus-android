package com.sloy.sevibus.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.sloy.sevibus.R
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.LineSummary
import com.sloy.sevibus.domain.model.Stop
import com.sloy.sevibus.domain.model.description1
import com.sloy.sevibus.domain.model.description2
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
fun StopCardElement(stop: Stop, isHighlighted: Boolean, onStopClick: (Stop) -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = { onStopClick(stop) },
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) SevTheme.colorScheme.surfaceVariant else SevTheme.colorScheme.surface,
        ),
    ) {
        ListItem(
            modifier = Modifier.padding(vertical = 8.dp),
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            headlineContent = {
                Column {
                    Text(stop.description1, style = SevTheme.typography.bodyStandardBold)
                    stop.description2?.let {
                        Text(it, style = SevTheme.typography.bodySmall, color = SevTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            supportingContent = {
                Text(stringResource(R.string.common_stop_with_code, stop.code), style = SevTheme.typography.bodyExtraSmall, modifier = Modifier.padding(top = 8.dp))
            },
            trailingContent = { SupportingLines(stop.lines) },
        )
    }
}

@Composable
private fun SupportingLines(lines: List<LineSummary>) {
    Row {
        lines.forEach { line ->
            LineIndicator(line = line)
            Spacer(Modifier.size(4.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun SingleLinePreview() {
    SevTheme {
        StopCardElement(Stubs.stops[0], false, {})
    }
}

@Preview(showBackground = true)
@Composable
internal fun DoubleLinePreview() {
    SevTheme {
        StopCardElement(Stubs.stops[1], false, {})
    }
}

@Preview(showBackground = true)
@Composable
internal fun HighlightedPreview() {
    SevTheme {
        StopCardElement(Stubs.stops.first(), true, {})
    }
}
