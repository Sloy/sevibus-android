package com.sloy.debugmenu.events.viewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.sloy.debugmenu.base.DebugPreviewTheme
import com.sloy.debugmenu.base.ScreenshotSuite
import com.sloy.debugmenu.base.ScreenshotTest
import com.sloy.debugmenu.events.CapturedEvent
import com.sloy.debugmenu.events.EventText
import com.sloy.debugmenu.events.EventType
import com.sloy.debugmenu.events.colors

private const val INLINE_PROPERTIES = 3
private const val CHEVRON_MILLIS = 200
private const val PULSE_MILLIS = 1600
private val MarkerCentre = 21.dp
private const val NO_BREAK_SPACE = '\u00A0'
private const val PROPERTY_SEPARATOR = "  "

@Composable
internal fun EventRow(
    event: CapturedEvent,
    delta: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    inBand: Boolean = false,
    isOldest: Boolean = false,
) {
    val hasProperties = event.properties.isNotEmpty()
    val isExpanded = expanded && hasProperties
    val rowBackground = if (isExpanded) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.025f) else Color.Transparent
    val chevron by animateFloatAsState(if (isExpanded) 180f else 0f, tween(CHEVRON_MILLIS), label = "rowChevron")
    Row(
        modifier
            .fillMaxWidth()
            .background(rowBackground)
            .then(if (hasProperties) Modifier.clickable(onClick = onToggle) else Modifier)
            .height(IntrinsicSize.Min)
            .padding(end = if (inBand) 8.dp else 16.dp),
    ) {
        Column(
            Modifier
                .width(if (inBand) 68.dp else 76.dp)
                .padding(start = if (inBand) 8.dp else 16.dp, top = 11.dp)
        ) {
            Text(event.timestamp, style = EventText.Mono12, color = MaterialTheme.colorScheme.onSurface)
            Text(delta, style = EventText.Mono10, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        TimelineMarker(event.type, lineBelow = !isOldest)
        Column(Modifier.weight(1f).padding(top = 11.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(event.name, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                if (hasProperties) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp).rotate(chevron),
                    )
                }
            }
            AnimatedVisibility(
                visible = hasProperties && !isExpanded,
                enter = InlineEnter,
                exit = InlineExit,
            ) {
                InlineProperties(event.properties, Modifier.padding(top = 4.dp))
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter = DetailsEnter,
                exit = DetailsExit,
            ) {
                PropertyBox(event.properties, event.timestampMillis, Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
private fun InlineProperties(properties: Map<String, String>, modifier: Modifier = Modifier) {
    val keyColor = MaterialTheme.colorScheme.onSurfaceVariant
    val valueColor = MaterialTheme.colorScheme.onSurface
    val text = buildAnnotatedString {
        properties.entries.take(INLINE_PROPERTIES).forEachIndexed { index, (key, value) ->
            if (index > 0) append(PROPERTY_SEPARATOR)
            withStyle(SpanStyle(color = keyColor)) { append(key) }
            append(NO_BREAK_SPACE)
            withStyle(SpanStyle(color = valueColor)) { append(value) }
        }
        if (properties.size > INLINE_PROPERTIES) {
            append(PROPERTY_SEPARATOR)
            withStyle(SpanStyle(color = keyColor)) { append("+${properties.size - INLINE_PROPERTIES}${NO_BREAK_SPACE}more") }
        }
    }
    Text(text, style = EventText.Mono12, modifier = modifier)
}

@Composable
internal fun TimelineMarker(type: EventType, modifier: Modifier = Modifier, lineBelow: Boolean = true) {
    val colors = type.colors()
    val lineColor = railLineColor()
    Box(modifier.width(24.dp).fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
        Box(Modifier.width(1.dp).height(MarkerCentre).background(lineColor))
        if (lineBelow) Box(Modifier.padding(top = MarkerCentre).width(1.dp).fillMaxHeight().background(lineColor))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(top = 11.dp)
                .size(20.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .background(colors.tint, CircleShape),
        ) {
            if (type == EventType.VIEW) {
                Icon(Icons.Outlined.PhoneAndroid, contentDescription = null, tint = colors.accent, modifier = Modifier.size(12.dp))
            } else {
                Box(Modifier.size(8.dp).background(colors.accent, CircleShape))
            }
        }
    }
}

@Composable
private fun railLineColor(): Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

/**
 * Pulsing tip on top of the Timeline rail, where new events come in.
 */
@Composable
internal fun TimelineHead(modifier: Modifier = Modifier) {
    val progress = if (LocalInspectionMode.current) {
        0f
    } else {
        val transition = rememberInfiniteTransition(label = "timelineHead")
        val animated by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(PULSE_MILLIS), RepeatMode.Restart),
            label = "timelineHeadProgress",
        )
        animated
    }
    val primary = MaterialTheme.colorScheme.primary
    val lineColor = railLineColor()
    Row(modifier.fillMaxWidth().height(24.dp)) {
        Box(Modifier.width(76.dp))
        Box(Modifier.width(24.dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
            Box(Modifier.padding(top = 12.dp).width(1.dp).fillMaxHeight().align(Alignment.BottomCenter).background(lineColor))
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
            Box(Modifier.size(8.dp).background(primary, CircleShape))
        }
        Text(
            "Now",
            style = EventText.Mono10,
            color = primary,
            modifier = Modifier.align(Alignment.CenterVertically).padding(start = 4.dp),
        )
    }
}

@ScreenshotTest(ScreenshotSuite.Components)
@PreviewLightDark
@Composable
internal fun EventRowCollapsedPreview() {
    DebugPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            EventRow(ViewerSampleData.events.first(), delta = "+2m 13s", expanded = false, onToggle = {})
        }
    }
}

@ScreenshotTest(ScreenshotSuite.Components)
@PreviewLightDark
@Composable
internal fun EventRowExpandedPreview() {
    DebugPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            val event = ViewerSampleData.events.first { it.id == ViewerSampleData.expandedEventId }
            EventRow(event, delta = "+0.60s", expanded = true, onToggle = {})
        }
    }
}

@ScreenshotTest(ScreenshotSuite.Components)
@PreviewLightDark
@Composable
internal fun EventRowNoPropertiesPreview() {
    DebugPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            EventRow(ViewerSampleData.events.first { it.name == "Map Explored" }, delta = "+0.75s", expanded = false, onToggle = {})
        }
    }
}
