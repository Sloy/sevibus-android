package com.sloydev.sevibus.ui.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.domain.model.Line
import com.sloydev.sevibus.domain.model.LineSummary
import com.sloydev.sevibus.domain.model.toSummary
import com.sloydev.sevibus.ui.theme.SevTheme

@Composable
fun LineIndicatorMedium(line: Line, modifier: Modifier = Modifier) {
    LineIndicatorMedium(line.toSummary(), modifier)
}

@Composable
fun LineIndicatorMedium(line: LineSummary, modifier: Modifier = Modifier) {
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
                style = SevTheme.typography.bodySmallBold
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
                    LineIndicatorMedium(line = it)
                }
            }
        }
    }
}
