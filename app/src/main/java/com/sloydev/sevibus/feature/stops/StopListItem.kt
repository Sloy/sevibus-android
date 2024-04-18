package com.sloydev.sevibus.feature.stops

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.feature.lines.Line
import com.sloydev.sevibus.feature.stops.ListPosition.End
import com.sloydev.sevibus.feature.stops.ListPosition.Middle
import com.sloydev.sevibus.feature.stops.ListPosition.Start
import com.sloydev.sevibus.ui.components.LineIndicatorSmall
import com.sloydev.sevibus.ui.theme.SevTheme


@Composable
fun StopListItem(
    number: Int,
    name: String,
    lines: List<Line>,
    listPosition: ListPosition,
    color: Color = Color.Blue,
) {
    TimelineNode(listPosition, color) {
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
            )
        }
    }
}

@Composable
private fun TimelineNode(listPosition: ListPosition, color: Color, content: @Composable () -> Unit) {
    val lineSize = 16.dp
    val linePadding = 16.dp
    val iconTopPadding = 42.dp

    val x = linePadding + lineSize / 2

    val iconSize = 8.dp

    Box(
        modifier = Modifier
            .drawBehind {
                val (lineStartY, lineEndY) = when (listPosition) {
                    Start -> iconTopPadding.toPx() to this.size.height
                    Middle -> 0f to this.size.height
                    End -> 0f to iconTopPadding.toPx()
                }
                drawLine(
                    cap = StrokeCap.Round,
                    color = color,
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
            LineIndicatorSmall(line = line)
            Spacer(Modifier.size(4.dp))
        }
    }
}

enum class ListPosition {
    Start, Middle, End
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun StopListItemPreview() {
    SevTheme {
        Scaffold {
            Column(Modifier.fillMaxSize()) {
                StopListItem(number = 512, name = "Avenida La Borbolla (Capitanía)", lines = Stubs.lines.take(3), Start)
                StopListItem(number = 512, name = "Avenida La Borbolla (Capitanía)", lines = Stubs.lines.take(3), Middle)
                StopListItem(number = 512, name = "Avenida La Borbolla (Capitanía)", lines = Stubs.lines.take(3), Middle)
                StopListItem(number = 512, name = "Avenida La Borbolla (Capitanía)", lines = Stubs.lines.take(3), End)
            }
        }
    }
}