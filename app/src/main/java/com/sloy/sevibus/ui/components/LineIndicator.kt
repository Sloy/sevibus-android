package com.sloy.sevibus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.Line
import com.sloy.sevibus.domain.model.LineSummary
import com.sloy.sevibus.domain.model.toSummary
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
fun LineIndicator(line: Line, modifier: Modifier = Modifier) {
    LineIndicator(line.toSummary(), modifier)
}

@Composable
fun LineIndicator(line: LineSummary, modifier: Modifier = Modifier) {
    SevTheme.WithLineColors(line.color) {
        Box(
            modifier
                .defaultMinSize(24.dp, 24.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(SevTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                line.label,
                color = SevTheme.colorScheme.onPrimary,
                style = SevTheme.typography.bodySmallBold,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
private fun Preview() {
    SevTheme {
        Surface {
            FlowRow(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Stubs.lines.forEach {
                    LineIndicator(line = it)
                }
            }
        }
    }
}
