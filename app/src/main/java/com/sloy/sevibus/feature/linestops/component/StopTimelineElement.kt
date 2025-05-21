package com.sloy.sevibus.feature.linestops.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.LineColor
import com.sloy.sevibus.domain.model.Stop
import com.sloy.sevibus.domain.model.primary
import com.sloy.sevibus.domain.model.soft
import com.sloy.sevibus.feature.linestops.component.ListPosition.End
import com.sloy.sevibus.feature.linestops.component.ListPosition.Middle
import com.sloy.sevibus.feature.linestops.component.ListPosition.Start
import com.sloy.sevibus.ui.components.StopCardElement
import com.sloy.sevibus.ui.icons.SevIcons
import com.sloy.sevibus.ui.icons.Stop
import com.sloy.sevibus.ui.icons.StopFilled
import com.sloy.sevibus.ui.theme.SevTheme


@Composable
fun StopTimelineElement(
    stop: Stop,
    highlightPosition: HighlightPosition,
    listPosition: ListPosition,
    color: LineColor,
    onStopClick: (Stop) -> Unit,
) {
    TimelineNode(listPosition, highlightPosition, color) {
        StopCardElement(
            stop,
            highlightPosition == HighlightPosition.Highlighted,
            onStopClick,
            Modifier
                .padding(vertical = 4.dp)
                .padding(end = 8.dp)
        )
    }
}

@Composable
private fun TimelineNode(
    listPosition: ListPosition,
    highlightPosition: HighlightPosition,
    color: LineColor,
    content: @Composable () -> Unit
) {
    val lineWidth = 8.dp
    val linePadding = 24.dp

    val lineCenterX = linePadding + lineWidth / 2

    val iconStrokeSize = 3.dp
    val iconSize = if (highlightPosition == HighlightPosition.Highlighted) 36.dp else 24.dp
    val iconSizeWithStroke = iconSize + iconStrokeSize * 2

    val (topColor, bottomColor) = when (highlightPosition) {
        HighlightPosition.BeforeHighlighted -> color.soft() to color.soft()
        HighlightPosition.Highlighted -> color.soft() to color.primary()
        HighlightPosition.AfterHighlighted,
        HighlightPosition.None -> color.primary() to color.primary()
    }
    val (showTop, showBottom) = when (listPosition) {
        Start -> false to true
        Middle -> true to true
        End -> true to false
    }

    Box(
        modifier = Modifier
            .drawBehind {
                if (showTop) {
                    drawLine(
                        cap = StrokeCap.Butt,
                        color = topColor,
                        start = Offset(x = lineCenterX.toPx(), y = 0f),
                        end = Offset(x = lineCenterX.toPx(), y = this.size.height / 2),
                        strokeWidth = lineWidth.toPx()
                    )
                }
                if (showBottom) {
                    drawLine(
                        cap = StrokeCap.Butt,
                        color = bottomColor,
                        start = Offset(x = lineCenterX.toPx(), y = this.size.height / 2),
                        end = Offset(x = lineCenterX.toPx(), y = this.size.height),
                        strokeWidth = lineWidth.toPx()
                    )
                }
            }
    ) {
        Icon(SevIcons.StopFilled, contentDescription = null, tint = SevTheme.colorScheme.background, modifier = Modifier
            .size(iconSizeWithStroke)
            .align(Alignment.CenterStart)
            .graphicsLayer {
                translationX = lineCenterX.toPx() - iconSizeWithStroke.toPx() / 2
            })
        Icon(SevIcons.Stop, contentDescription = null, tint = color.primary(), modifier = Modifier
            .size(iconSizeWithStroke)
            .padding(iconStrokeSize)
            .align(Alignment.CenterStart)
            .graphicsLayer {
                translationX = lineCenterX.toPx() - iconSizeWithStroke.toPx() / 2
            })
        Box(Modifier.padding(start = linePadding * 2 + lineWidth)) {
            content()
        }
    }
}

enum class ListPosition {
    Start, Middle, End
}

enum class HighlightPosition {
    None, BeforeHighlighted, Highlighted, AfterHighlighted
}

@PreviewLightDark
@Composable
private fun StopListItemPreview() {
    SevTheme {
        Column(
            Modifier
                .fillMaxSize()
                .padding(top = 48.dp)
        ) {
            StopTimelineElement(Stubs.stops[0], HighlightPosition.BeforeHighlighted, Start, LineColor.Green) { }
            StopTimelineElement(Stubs.stops[1], HighlightPosition.BeforeHighlighted, Middle, LineColor.Green) { }
            StopTimelineElement(Stubs.stops[2], HighlightPosition.Highlighted, Middle, LineColor.Green) { }
            StopTimelineElement(Stubs.stops[3], HighlightPosition.AfterHighlighted, End, LineColor.Green) { }
        }
    }
}
