package com.sloydev.sevibus.feature.stops

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.feature.lines.Line
import com.sloydev.sevibus.feature.stops.Position.*
import com.sloydev.sevibus.ui.theme.SevTheme


@Composable
private fun StopItem(number: Int, name: String, lines: List<Line>, position: Position) {
    TimelineNode(position) {
        Card(
            Modifier
                .padding(vertical = 4.dp)
                .padding(end = 8.dp)
        ) {
            ListItem(
                tonalElevation = 8.dp,
                headlineContent = { Text(name) },
                overlineContent = { Text(text = number.toString()) },
                supportingContent = { SupportingLines(lines) },
                //trailingContent = { Text(text = "trailing") },
            )
        }
    }
}

@Composable
private fun TimelineNode(position: Position, content: @Composable () -> Unit) {
    val lineSize = 16.dp
    val linePadding = 16.dp
    val iconTopPadding = 42.dp

    val x = linePadding + lineSize / 2

    val iconSize = 8.dp

    val lineColor = Color.Blue

    Box(
        modifier = Modifier
            .drawBehind {
                val (lineStartY, lineEndY) = when (position) {
                    Start -> iconTopPadding.toPx() to this.size.height
                    Middle -> 0f to this.size.height
                    End -> 0f to iconTopPadding.toPx()
                }
                drawLine(
                    cap = StrokeCap.Round,
                    color = lineColor,
                    start = Offset(x = x.toPx(), y = lineStartY),
                    end = Offset(x = x.toPx(), y = lineEndY),
                    strokeWidth = lineSize.toPx()
                )
                drawCircle(
                    Color.White,
                    radius = iconSize.toPx() / 2,
                    center = Offset(x = x.toPx(), y = iconTopPadding.toPx())
                )
            }

    ) {
        Box(
            Modifier.padding(
                start = linePadding * 2 + lineSize,
            )
        ) {
            content()
        }
    }
}

@Composable
private fun SupportingLines(lines: List<Line>) {
    Row {
        lines.forEach { line ->
            LineIndicatorSmall(it = line)
            Spacer(Modifier.size(4.dp))
        }
    }
}

@Composable
private fun LineIndicatorSmall(it: Line) {
    Box(
        Modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(Color(it.colorHex))
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            it.label,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.bodySmall
                .copy(fontWeight = FontWeight.Medium)
        )
    }
}

private enum class Position {
    Start, Middle, End
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun StopItemPreview() {
    SevTheme {
        Scaffold {
            Column(Modifier.fillMaxSize()) {
                StopItem(number = 512, name = "Avenida La Borbolla (Capitanía)", lines = Stubs.lines.take(3), Start)
                StopItem(number = 512, name = "Avenida La Borbolla (Capitanía)", lines = Stubs.lines.take(3), Middle)
                StopItem(number = 512, name = "Avenida La Borbolla (Capitanía)", lines = Stubs.lines.take(3), Middle)
                StopItem(number = 512, name = "Avenida La Borbolla (Capitanía)", lines = Stubs.lines.take(3), End)
            }
        }
    }
}