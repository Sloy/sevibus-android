package com.sloy.debugmenu.events.overlay

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.sloy.debugmenu.base.ScreenshotSuite
import com.sloy.debugmenu.base.ScreenshotTest
import com.sloy.debugmenu.events.CapturedEvent
import com.sloy.debugmenu.events.EventText
import com.sloy.debugmenu.events.EventType

private const val ORIGIN = 6f
private const val STRIP_END = 12f
private const val STRIP_WIDTH = 18f
private const val CENTRE_END = STRIP_END + STRIP_WIDTH / 2
private const val LABEL_END = CENTRE_END + 14f
private const val MARKER_RING = 1.5f

private fun Modifier.atRailY(y: Float, height: Float) = graphicsLayer { translationY = -(ORIGIN + y - height / 2).dp.toPx() }

@Composable
internal fun EventsRailOverlay(events: List<CapturedEvent>, nowMillis: Long, modifier: Modifier = Modifier) {
    val frame = railFrame(events, nowMillis)
    Box(modifier.fillMaxSize().offset(y = 10.dp)) {
        RailStrip(Modifier.align(Alignment.BottomEnd).padding(end = STRIP_END.dp))
        frame.bands.forEach { band ->
            key(band.key) {
                val height = band.top - band.bottom
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = STRIP_END.dp)
                        .atRailY((band.top + band.bottom) / 2, height)
                        .graphicsLayer { alpha = band.alpha }
                        .size(STRIP_WIDTH.dp, height.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(OverlayColors.RailBandFill)
                        .border(1.dp, OverlayColors.RailBandBorder, RoundedCornerShape(9.dp))
                )
            }
        }
        frame.markers.asReversed().forEach { marker ->
            key(marker.key) { RailEvent(marker) }
        }
        NowHead(Modifier.align(Alignment.BottomEnd).padding(end = (CENTRE_END - 4f).dp))
    }
}

@Composable
private fun RailStrip(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(9.dp)
    Box(
        modifier
            .size(STRIP_WIDTH.dp, (RailSpec.RAIL_HEIGHT + 12).dp)
            .clip(shape)
            .background(OverlayColors.RailTrack)
            .border(1.dp, OverlayColors.RailTrackBorder, shape),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.width(1.dp).fillMaxHeight().padding(vertical = 6.dp).background(OverlayColors.RailLine))
    }
}

@Composable
private fun BoxScope.RailEvent(marker: RailMarker) {
    if (marker.linkLength > 0f) {
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(end = (CENTRE_END - 1.5f).dp)
                .atRailY(marker.y - marker.linkLength / 2, marker.linkLength)
                .graphicsLayer { alpha = marker.markerAlpha }
                .size(3.dp, marker.linkLength.dp)
                .background(OverlayColors.RailLink)
        )
    }

    val (width, height) = marker.type.markerSize()
    if (marker.markerScale > 0f) {
        RailMarkerShape(
            type = marker.type,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = (CENTRE_END - width / 2 - MARKER_RING).dp)
                .atRailY(marker.y, height + 2 * MARKER_RING)
                .graphicsLayer {
                    scaleX = marker.markerScale
                    scaleY = marker.markerScale
                    alpha = marker.markerAlpha
                    rotationZ = if (marker.type == EventType.CLICK) 45f else 0f
                },
        )
    }

    if (marker.labelVisible) {
        RailLabel(
            marker,
            Modifier
                .align(Alignment.BottomEnd)
                .padding(end = LABEL_END.dp)
                .atRailY(marker.y, 24f)
                .graphicsLayer {
                    alpha = marker.labelAlpha
                    translationX = (1f - marker.labelProgress) * 10.dp.toPx()
                    val scale = 0.6f + 0.4f * marker.labelProgress
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(1f, 0.5f)
                },
        )
    }
}

private fun EventType.markerSize(): Pair<Float, Float> = when (this) {
    EventType.VIEW -> 10f to 14f
    EventType.CLICK -> 10f to 10f
    EventType.OTHER -> 11f to 11f
}

@Composable
private fun RailMarkerShape(type: EventType, modifier: Modifier = Modifier) {
    val (width, height) = type.markerSize()
    val (outer, inner) = when (type) {
        EventType.VIEW -> RoundedCornerShape((3f + MARKER_RING).dp) to RoundedCornerShape(3.dp)
        EventType.CLICK -> RoundedCornerShape((2f + MARKER_RING).dp) to RoundedCornerShape(2.dp)
        EventType.OTHER -> CircleShape to CircleShape
    }
    Box(
        modifier
            .size((width + 2 * MARKER_RING).dp, (height + 2 * MARKER_RING).dp)
            .shadow(1.dp, outer, ambientColor = OverlayColors.MarkerShadow)
            .background(OverlayColors.MarkerRing, outer)
            .padding(MARKER_RING.dp)
    ) {
        if (type == EventType.VIEW) {
            Box(Modifier.size(width.dp, height.dp).background(Color.White, inner).border(2.dp, EventType.VIEW.color, inner))
        } else {
            Box(Modifier.size(width.dp, height.dp).background(type.color, inner))
        }
    }
}

@Composable
private fun RailLabel(marker: RailMarker, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(OverlayColors.Glass)
            .padding(start = 8.dp, end = 9.dp),
    ) {
        Box(Modifier.size(7.dp).background(marker.type.onDark, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(
            marker.name,
            style = EventText.Mono12Medium,
            color = OverlayColors.OnGlass,
            maxLines = 1,
            overflow = TextOverflow.StartEllipsis,
            modifier = Modifier.widthIn(max = 300.dp),
        )
    }
}

@Composable
private fun NowHead(modifier: Modifier = Modifier) {
    val isStatic = LocalInspectionMode.current
    val progress = if (isStatic) {
        0f
    } else {
        val transition = rememberInfiniteTransition(label = "nowHead")
        val animated by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1600, easing = OverlayEasing.CssEaseOut), RepeatMode.Restart),
            label = "nowHeadProgress",
        )
        animated
    }
    val primary = MaterialTheme.colorScheme.primary
    Box(modifier.size(8.dp).atRailY(0f, 8f), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(8.dp)
                .graphicsLayer {
                    alpha = 0.55f * (1f - progress)
                    scaleX = 1f + 1.6f * progress
                    scaleY = 1f + 1.6f * progress
                }
                .background(primary, CircleShape)
        )
        Box(Modifier.size(6.dp).background(primary, CircleShape))
    }
}

@ScreenshotTest(ScreenshotSuite.Screens)
@PreviewLightDark
@Composable
internal fun EventsRailOverlayDemoPreview() {
    OverlayPreviewBackground { EventsRailOverlay(OverlayDemo.events(0), nowMillis = 10_900) }
}

@ScreenshotTest(ScreenshotSuite.Screens)
@PreviewLightDark
@Composable
internal fun EventsRailOverlayLaterPreview() {
    OverlayPreviewBackground { EventsRailOverlay(OverlayDemo.events(0), nowMillis = 12_300) }
}
