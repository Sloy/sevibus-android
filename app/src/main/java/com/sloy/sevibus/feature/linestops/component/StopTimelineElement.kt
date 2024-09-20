package com.sloy.sevibus.feature.linestops.component

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.Stop
import com.sloy.sevibus.feature.linestops.component.ListPosition.End
import com.sloy.sevibus.feature.linestops.component.ListPosition.Middle
import com.sloy.sevibus.feature.linestops.component.ListPosition.Start
import com.sloy.sevibus.ui.components.StopCardElement
import com.sloy.sevibus.ui.theme.AlexGreen
import com.sloy.sevibus.ui.theme.SevTheme


@Composable
fun StopTimelineElement(
    stop: Stop,
    listPosition: ListPosition,
    color: Color = AlexGreen,
    onStopClick: (Stop) -> Unit,
) {
    TimelineNode(listPosition, color) {
        StopCardElement(
            stop, onStopClick,
            Modifier
                .padding(vertical = 4.dp)
                .padding(end = 8.dp)
        )
    }
}

@Composable
private fun TimelineNode(listPosition: ListPosition, color: Color, content: @Composable () -> Unit) {
    val lineSize = 8.dp
    val linePadding = 16.dp
    val iconTopPadding = 42.dp

    val x = linePadding + lineSize / 2

    val iconSize = 8.dp
    val outerIconSize = 24.dp

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
                    color,
                    radius = outerIconSize.toPx() / 2,
                    center = Offset(x = x.toPx(), y = iconTopPadding.toPx())
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
                StopTimelineElement(Stubs.stops[0], Start) { }
                StopTimelineElement(Stubs.stops[1], Middle) { }
                StopTimelineElement(Stubs.stops[2], Middle) { }
                StopTimelineElement(Stubs.stops[3], End) { }
            }
        }
    }
}