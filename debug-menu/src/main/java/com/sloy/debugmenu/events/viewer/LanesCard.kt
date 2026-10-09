package com.sloy.debugmenu.events.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sloy.debugmenu.base.DebugPreviewTheme
import com.sloy.debugmenu.base.ScreenshotSuite
import com.sloy.debugmenu.base.ScreenshotTest
import com.sloy.debugmenu.events.EventText
import com.sloy.debugmenu.events.EventType
import com.sloy.debugmenu.events.colors

private const val TAP_TOLERANCE_DP = 24f

@Composable
internal fun LanesCard(model: LanesModel, onMarkClick: (String) -> Unit, modifier: Modifier = Modifier) {
    val view = EventType.VIEW.colors()
    val click = EventType.CLICK.colors()
    val other = EventType.OTHER.colors()
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                model.range,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text("${model.span} · follows scroll", style = EventText.Mono11, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(8.dp))
        LaneRow("Screens", view.ink, Lane.SCREENS, model, onMarkClick) { width ->
            model.screens.forEach { bar ->
                val barWidth = width * (bar.end - bar.start) - 2.dp
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .offset(x = width * bar.start)
                        .width(barWidth.coerceAtLeast(2.dp))
                        .height(22.dp)
                        .background(view.tint, RoundedCornerShape(6.dp)),
                ) {
                    if (barWidth >= 44.dp) {
                        Text(bar.label, style = MaterialTheme.typography.labelMedium, color = view.ink, maxLines = 1, overflow = TextOverflow.Clip)
                    }
                }
            }
        }
        LaneRow("Clicks", click.ink, Lane.CLICKS, model, onMarkClick) { width ->
            model.clicks.forEach { mark ->
                Box(Modifier.offset(x = width * mark.x - 4.5.dp).size(9.dp).rotate(45f).background(click.accent, RoundedCornerShape(1.dp)))
            }
        }
        LaneRow("Events", other.ink, Lane.EVENTS, model, onMarkClick) { width ->
            model.events.forEach { mark ->
                Box(Modifier.offset(x = width * mark.x - 4.dp).size(8.dp).background(other.accent, CircleShape))
            }
        }
        Row(Modifier.padding(start = 50.dp, top = 4.dp)) {
            BoxWithConstraints(Modifier.weight(1f)) {
                model.axis.forEach { (fraction, label) ->
                    Text(
                        label,
                        style = EventText.Mono9,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.offset(x = maxWidth * fraction - 10.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun LaneRow(
    caption: String,
    captionColor: Color,
    lane: Lane,
    model: LanesModel,
    onMarkClick: (String) -> Unit,
    marks: @Composable BoxScope.(width: Dp) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(30.dp)) {
        Text(caption, style = MaterialTheme.typography.labelMedium, color = captionColor, modifier = Modifier.width(50.dp))
        BoxWithConstraints(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .pointerInput(model) {
                    detectTapGestures { offset ->
                        model.markAt(lane, offset.x / size.width, TAP_TOLERANCE_DP.dp.toPx() / size.width)?.let(onMarkClick)
                    }
                },
        ) { marks(maxWidth) }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@ScreenshotTest(ScreenshotSuite.Components)
@PreviewLightDark
@Composable
internal fun LanesCardPreview() {
    DebugPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            val start = ViewerSampleData.startMillis
            LanesCard(lanesModel(ViewerSampleData.events, start + 1_050, start + 15_000), onMarkClick = {})
        }
    }
}
