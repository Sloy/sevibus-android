package com.sloy.debugmenu.events.overlay

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val ENTER_SHIFT = 12f

@Composable
internal fun EventsStackOverlay(
    events: List<CapturedEvent>,
    nowMillis: Long,
    modifier: Modifier = Modifier,
    lifetimeMillis: Long = StackSpec.LIFETIME_MILLIS,
) {
    val isStatic = LocalInspectionMode.current
    val offsets = remember { HashMap<String, Animatable<Float, AnimationVector1D>>() }
    BoxWithConstraints(modifier.fillMaxSize()) {
        val frame = stackFrame(events, nowMillis, maxHeight.value - StackSpec.TOP_RESERVE, lifetimeMillis)
        val enterShift = enterShift(frame.chips, offsets)
        frame.chips.asReversed().forEach { chip ->
            key(chip.key) {
                val offsetY = offsets.getOrPut(chip.key) { Animatable(if (isStatic) chip.y else chip.y + enterShift) }
                DisposableEffect(Unit) { onDispose { offsets.remove(chip.key) } }
                StackChipItem(chip, offsetY, Modifier.align(Alignment.BottomEnd).padding(end = 12.dp))
            }
        }
        OlderPill(frame.olderCount, frame.olderY, Modifier.align(Alignment.BottomEnd).padding(end = 12.dp))
    }
}

/**
 * How far below its slot a new chip starts: as far as the chips already shown still have to move up,
 * so they all travel together and never overlap.
 */
private fun enterShift(chips: List<StackChip>, offsets: Map<String, Animatable<Float, AnimationVector1D>>): Float {
    val shown = chips.firstOrNull { it.foldIndex == null && it.key in offsets } ?: return ENTER_SHIFT
    return maxOf(offsets.getValue(shown.key).value - shown.y, ENTER_SHIFT)
}

@Composable
private fun StackChipItem(chip: StackChip, offsetY: Animatable<Float, AnimationVector1D>, modifier: Modifier) {
    val isStatic = LocalInspectionMode.current
    val scale = remember { Animatable(if (isStatic) chip.scale else 0.5f) }
    val alpha = remember { Animatable(if (isStatic) chip.alpha else 0f) }
    val exitX = remember { Animatable(0f) }
    var previousCount by remember { mutableIntStateOf(chip.count) }

    LaunchedEffect(chip.y) { offsetY.animateTo(chip.y, tween(440, easing = OverlayEasing.SpringyOut)) }
    LaunchedEffect(chip.scale) { scale.animateTo(chip.scale, tween(440, easing = OverlayEasing.SpringyOut)) }
    LaunchedEffect(chip.alpha, chip.phase) {
        val spec = if (chip.phase == ChipPhase.OUT) tween<Float>(240, easing = OverlayEasing.CssEaseIn) else tween(250, easing = LinearEasing)
        alpha.animateTo(chip.alpha, spec)
    }
    LaunchedEffect(chip.phase) {
        if (chip.phase == ChipPhase.OUT) exitX.animateTo(40f, tween(320, easing = OverlayEasing.ExitAccel))
    }
    LaunchedEffect(chip.count) {
        if (chip.count > previousCount) {
            launch { scale.animateTo(1.08f, tween(440, easing = OverlayEasing.SpringyOut)) }
            delay(150)
            scale.animateTo(chip.scale, tween(440, easing = OverlayEasing.SpringyOut))
        }
        previousCount = chip.count
    }

    StackChipContent(
        chip = chip,
        modifier = modifier.graphicsLayer {
            translationY = offsetY.value.dp.toPx()
            translationX = exitX.value.dp.toPx()
            scaleX = scale.value
            scaleY = scale.value
            this.alpha = alpha.value
            transformOrigin = TransformOrigin(1f, 0.5f)
        },
    )
}

@Composable
private fun StackChipContent(chip: StackChip, modifier: Modifier = Modifier) {
    val accent = chip.type.onDark
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(28.dp)
            .widthIn(max = 330.dp)
            .clip(CircleShape)
            .background(OverlayColors.Glass)
            .drawBehind {
                val inset = 12.dp.toPx()
                val lineHeight = 2.dp.toPx()
                val width = size.width - inset * 2
                val top = size.height - lineHeight
                drawRect(OverlayColors.TimerTrack, Offset(inset, top), Size(width, lineHeight))
                drawRect(accent, Offset(inset, top), Size(width * chip.timerFraction, lineHeight))
            }
            .padding(start = 10.dp, end = 11.dp),
    ) {
        Box(Modifier.size(8.dp).background(accent, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(
            chip.name,
            style = EventText.Mono12Medium,
            color = OverlayColors.OnGlass,
            maxLines = 1,
            overflow = TextOverflow.StartEllipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (chip.count > 1) {
            Spacer(Modifier.width(8.dp))
            Text(
                "×${chip.count}",
                style = EventText.Mono11Medium,
                color = OverlayColors.OnGlass,
                modifier = Modifier
                    .background(OverlayColors.Badge, RoundedCornerShape(6.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun OlderPill(count: Int, y: Float, modifier: Modifier) {
    val offsetY by animateFloatAsState(y, tween(440, easing = OverlayEasing.SpringyOut), label = "olderY")
    val alpha by animateFloatAsState(if (count > 0) 1f else 0f, tween(200, easing = OverlayEasing.CssEase), label = "olderAlpha")
    if (count == 0 && alpha == 0f) return
    Text(
        "+$count older",
        style = EventText.Mono10Medium,
        color = OverlayColors.OnGlass,
        modifier = modifier
            .graphicsLayer {
                translationY = offsetY.dp.toPx()
                this.alpha = alpha
            }
            .height(20.dp)
            .background(OverlayColors.GlassMuted, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp)
            .wrapContentHeight(Alignment.CenterVertically),
    )
}

@ScreenshotTest(ScreenshotSuite.Screens)
@PreviewLightDark
@Composable
internal fun EventsStackOverlayDemoPreview() {
    OverlayPreviewBackground { EventsStackOverlay(OverlayDemo.events(0), nowMillis = 10_600) }
}

@ScreenshotTest(ScreenshotSuite.Screens)
@PreviewLightDark
@Composable
internal fun EventsStackOverlayLaterPreview() {
    OverlayPreviewBackground { EventsStackOverlay(OverlayDemo.events(0), nowMillis = 12_300) }
}

@ScreenshotTest(ScreenshotSuite.Screens)
@PreviewLightDark
@Composable
internal fun EventsStackOverlayOverflowPreview() {
    OverlayPreviewBackground { EventsStackOverlay(OverlayDemo.events(0), nowMillis = 12_600, lifetimeMillis = 20_000) }
}
