package com.sloydev.sevibus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.domain.model.Stop
import com.sloydev.sevibus.domain.model.description1
import com.sloydev.sevibus.domain.model.description2
import com.sloydev.sevibus.ui.theme.SevTheme

@Composable
fun StopDetailInfoItem(
    stop: Stop,
    modifier: Modifier = Modifier,
    showStopCode: Boolean = false,
) {
    Surface(shape = SevTheme.shapes.medium, modifier = modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp)) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = SevTheme.colorScheme.primary)
            Spacer(Modifier.size(16.dp))
            Column {
                Text(stop.description1, style = SevTheme.typography.bodyStandardBold)
                stop.description2?.let {
                    Text(it, style = SevTheme.typography.bodySmall, color = SevTheme.colorScheme.onSurfaceVariant)
                }
                if (showStopCode) {
                    Text(
                        "Parada ${stop.code}",
                        style = SevTheme.typography.bodyExtraSmall,
                        color = SevTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}


@Preview()
@Composable
private fun Preview() {
    SevTheme {
        Column(Modifier.background(SevTheme.colorScheme.background)) {
            StopDetailInfoItem(Stubs.stops[0], Modifier.padding(16.dp))
            StopDetailInfoItem(Stubs.stops[1], Modifier.padding(16.dp))
            StopDetailInfoItem(Stubs.stops[0], Modifier.padding(16.dp), showStopCode = true)
            StopDetailInfoItem(Stubs.stops[1], Modifier.padding(16.dp), showStopCode = true)

        }
    }
}
