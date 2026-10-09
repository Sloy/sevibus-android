package com.sloy.debugmenu.events.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
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
import com.sloy.debugmenu.events.formatDuration
import com.sloy.debugmenu.events.toClockTime

private const val TAP_TOLERANCE = 16f
private const val CAPTION_WIDTH = 50f
private const val LANE_HEIGHT = 30f
private const val AXIS_HEIGHT = 16f
private const val NEWEST_MARGIN = 24f
private const val OFFSCREEN_MARGIN = 60f

/**
 * Scrubbable lanes over [strip]. The highlighted window spans [fromMillis] to [toMillis], the times shown in the list,
 * and dragging horizontally reports the drag through [onScrub], in dp, positive when moving to older times.
 */
@Composable
internal fun LanesCard(
    strip: LanesStrip,
    fromMillis: Long,
    toMillis: Long,
    onScrub: (Float) -> Unit,
    onMarkClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${fromMillis.toClockTime()} → ${toMillis.toClockTime()}",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text("${formatDuration(toMillis - fromMillis)} · drag to scrub", style = EventText.Mono11, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(8.dp))
        Row {
            LaneCaptions()
            Spacer(Modifier.width(6.dp))
            LanesViewport(strip, fromMillis, toMillis, onScrub, onMarkClick, Modifier.weight(1f))
        }
    }
}

@Composable
private fun LaneCaptions() {
    Column(Modifier.width(CAPTION_WIDTH.dp)) {
        listOf(
            "Screens" to EventType.VIEW.colors().ink,
            "Clicks" to EventType.CLICK.colors().ink,
            "Events" to EventType.OTHER.colors().ink,
        ).forEach { (caption, color) ->
            Box(Modifier.height(LANE_HEIGHT.dp), contentAlignment = Alignment.CenterStart) {
                Text(caption, style = MaterialTheme.typography.labelMedium, color = color)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun LanesViewport(
    strip: LanesStrip,
    fromMillis: Long,
    toMillis: Long,
    onScrub: (Float) -> Unit,
    onMarkClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val currentOnScrub = rememberUpdatedState(onScrub)
    val dragState = rememberDraggableState { deltaPx -> currentOnScrub.value(with(density) { deltaPx.toDp().value }) }
    BoxWithConstraints(
        modifier
            .clipToBounds()
            .draggable(dragState, Orientation.Horizontal)
    ) {
        val viewport = maxWidth.value
        val windowStart = strip.xAt(fromMillis)
        val windowEnd = strip.xAt(toMillis)
        val scroll = (windowEnd - viewport + NEWEST_MARGIN).coerceIn(0f, (strip.width - viewport).coerceAtLeast(0f))
        fun visible(from: Float, to: Float) = to >= scroll - OFFSCREEN_MARGIN && from <= scroll + viewport + OFFSCREEN_MARGIN
        fun at(x: Float): Dp = (x - scroll).dp

        val totalHeight = (LANE_HEIGHT + 1) * 3
        strip.breaks.filter { visible(it.start, it.end) }.forEach { lanesBreak ->
            Box(
                Modifier
                    .offset(x = at(lanesBreak.start))
                    .size((lanesBreak.end - lanesBreak.start).dp, totalHeight.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            )
        }
        Box(
            Modifier
                .offset(x = at(windowStart))
                .size((windowEnd - windowStart).coerceAtLeast(2f).dp, totalHeight.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
        )
        Column {
            LaneRow(Lane.SCREENS, strip, scroll, onMarkClick) {
                val view = EventType.VIEW.colors()
                strip.screens.filter { visible(it.start, it.end) }.forEach { bar ->
                    val barWidth = (bar.end - bar.start - 2f).coerceAtLeast(2f)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .offset(x = at(bar.start))
                            .size(barWidth.dp, 22.dp)
                            .background(view.tint, RoundedCornerShape(6.dp)),
                    ) {
                        barLabel(bar.label, barWidth)?.let { label ->
                            Text(label, style = MaterialTheme.typography.labelMedium, color = view.ink, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false, modifier = Modifier.padding(horizontal = 2.dp))
                        }
                    }
                }
            }
            LaneRow(Lane.CLICKS, strip, scroll, onMarkClick) {
                val click = EventType.CLICK.colors()
                strip.clicks.filter { visible(it.x, it.x) }.forEach { mark ->
                    Box(Modifier.offset(x = at(mark.x) - 4.5.dp).size(9.dp).rotate(45f).background(click.accent, RoundedCornerShape(1.dp)))
                }
            }
            LaneRow(Lane.EVENTS, strip, scroll, onMarkClick) {
                val other = EventType.OTHER.colors()
                strip.events.filter { visible(it.x, it.x) }.forEach { mark ->
                    Box(Modifier.offset(x = at(mark.x) - 4.dp).size(8.dp).background(other.accent, CircleShape))
                }
            }
            Box(Modifier.fillMaxWidth().height(AXIS_HEIGHT.dp)) {
                strip.ticks.filter { visible(it.x, it.x) }.forEach { tick ->
                    AxisLabel(tick.label, Modifier.offset(x = at(tick.x) - 20.dp))
                }
                strip.breaks.filter { visible(it.start, it.end) }.forEach { lanesBreak ->
                    AxisLabel(lanesBreak.label, Modifier.offset(x = at((lanesBreak.start + lanesBreak.end) / 2) - 20.dp))
                }
            }
        }
    }
}

@Composable
private fun AxisLabel(text: String, modifier: Modifier) {
    Text(
        text,
        style = EventText.Mono9,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        maxLines = 1,
        modifier = modifier.width(40.dp).padding(top = 4.dp),
    )
}

@Composable
private fun LaneRow(lane: Lane, strip: LanesStrip, scroll: Float, onMarkClick: (String) -> Unit, marks: @Composable () -> Unit) {
    val density = LocalDensity.current
    val currentScroll = rememberUpdatedState(scroll)
    val currentOnMarkClick = rememberUpdatedState(onMarkClick)
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .height(LANE_HEIGHT.dp)
            .pointerInput(strip, lane) {
                detectTapGestures { offset ->
                    val x = with(density) { offset.x.toDp().value } + currentScroll.value
                    strip.markAt(lane, x, TAP_TOLERANCE)?.let(currentOnMarkClick.value)
                }
            },
    ) {
        Box(Modifier.fillMaxHeight(), contentAlignment = Alignment.CenterStart) { marks() }
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
            LanesCard(lanesStrip(ViewerSampleData.events), start + 7_500, start + 15_000, onScrub = {}, onMarkClick = {})
        }
    }
}
