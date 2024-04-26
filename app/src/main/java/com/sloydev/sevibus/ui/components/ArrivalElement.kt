package com.sloydev.sevibus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.domain.model.LineSummary
import com.sloydev.sevibus.domain.model.toSummary
import com.sloydev.sevibus.ui.theme.AlexGreySurface2
import com.sloydev.sevibus.ui.theme.SevTheme


@Composable
fun ArrivalElement(minutes: Int, line: LineSummary? = null) {
    Box(
        Modifier
            .clip(MaterialTheme.shapes.small)
            .background(AlexGreySurface2)
    ) {
        val text = if (minutes > 0) "$minutes min" else "Llegando..."
        if (line != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LineIndicatorSmall(line, modifier = Modifier.padding(4.dp))
                Text(
                    text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 2.dp, end = 6.dp)
                )
            }
        }else {
            Text(
                text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    SevTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(32.dp)
        ) {
            ArrivalElement(minutes = 15)
            ArrivalElement(minutes = 0)

            ArrivalElement(minutes = 15, line = Stubs.lines[0].toSummary())
            ArrivalElement(minutes = 0, line = Stubs.lines[6].toSummary())
        }
    }
}