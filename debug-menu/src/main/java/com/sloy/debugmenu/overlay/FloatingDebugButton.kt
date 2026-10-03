package com.sloy.debugmenu.overlay

import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.sloy.debugmenu.base.DebugPreviewTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
internal fun FloatingDebugButton(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val store = remember { FloatingButtonPositionStore(context.applicationContext) }
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical))
    ) {
        val buttonSizePx = with(density) { BUTTON_SIZE.toPx() }
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val initialPosition = remember { store.read() ?: StoredPosition(Edge.RIGHT, DEFAULT_Y_FRACTION) }
        var edge by remember { mutableStateOf(initialPosition.edge) }
        var yFraction by remember { mutableStateOf(initialPosition.yFraction) }
        val currentWidthPx by rememberUpdatedState(widthPx)
        val currentHeightPx by rememberUpdatedState(heightPx)
        val currentVisible by rememberUpdatedState(visible)
        val offsetX = remember { Animatable(hiddenX(initialPosition.edge, buttonSizePx, widthPx)) }
        val offsetY = remember { Animatable(clampY(initialPosition.yFraction * heightPx, buttonSizePx, 0f, heightPx)) }
        val coroutineScope = rememberCoroutineScope()
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        var isDragging by remember { mutableStateOf(false) }

        LaunchedEffect(visible, widthPx, heightPx) {
            offsetY.snapTo(clampY(yFraction * heightPx, buttonSizePx, 0f, heightPx))
            if (visible) {
                offsetX.animateTo(snapX(edge, buttonSizePx, widthPx), tween(SLIDE_IN_MILLIS, easing = OvershootEasing))
            } else {
                offsetX.animateTo(hiddenX(edge, buttonSizePx, widthPx), tween(SLIDE_OUT_MILLIS, easing = AnticipateEasing))
            }
        }

        suspend fun settle() {
            val width = currentWidthPx
            val height = currentHeightPx
            val newEdge = nearestEdge(offsetX.value, buttonSizePx, width)
            edge = newEdge
            if (height > 0f) {
                yFraction = (offsetY.value / height).coerceIn(0f, 1f)
                store.write(StoredPosition(newEdge, yFraction))
            }
            val targetX = if (currentVisible) snapX(newEdge, buttonSizePx, width) else hiddenX(newEdge, buttonSizePx, width)
            offsetX.animateTo(
                targetX,
                spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
            )
        }

        FloatingDebugButtonContent(
            pressed = isPressed || isDragging,
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
                .pointerInput(Unit) {
                    try {
                        detectDragGestures(
                            onDragStart = {
                                isDragging = true
                                coroutineScope.launch {
                                    offsetX.stop()
                                    offsetY.stop()
                                }
                            },
                            onDragEnd = {
                                isDragging = false
                                coroutineScope.launch { settle() }
                            },
                            onDragCancel = {
                                isDragging = false
                                coroutineScope.launch { settle() }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                coroutineScope.launch {
                                    offsetX.snapTo((offsetX.value + dragAmount.x).coerceIn(-buttonSizePx, currentWidthPx))
                                    offsetY.snapTo(clampY(offsetY.value + dragAmount.y, buttonSizePx, 0f, currentHeightPx))
                                }
                            },
                        )
                    } finally {
                        isDragging = false
                    }
                }
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        )
    }
}

@Composable
internal fun FloatingDebugButtonContent(pressed: Boolean, modifier: Modifier = Modifier) {
    val scale by animateFloatAsState(
        targetValue = if (pressed) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 1f, stiffness = 800f),
        label = "floatingButtonScale",
    )
    val elevation by animateDpAsState(
        targetValue = if (pressed) 12.dp else 6.dp,
        animationSpec = spring(dampingRatio = 1f, stiffness = 800f),
        label = "floatingButtonElevation",
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(BUTTON_SIZE)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation, CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
    ) {
        Icon(
            Icons.Filled.BugReport,
            contentDescription = "Open debug menu",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp),
        )
    }
}

private val BUTTON_SIZE = 48.dp
private const val DEFAULT_Y_FRACTION = 0.25f
private const val SLIDE_IN_MILLIS = 280
private const val SLIDE_OUT_MILLIS = 220
private val OvershootEasing = Easing { OvershootInterpolator(1.5f).getInterpolation(it) }
private val AnticipateEasing = Easing { AnticipateInterpolator(1.5f).getInterpolation(it) }

@PreviewLightDark
@Composable
private fun FloatingDebugButtonPreview() {
    DebugPreviewTheme {
        Row(Modifier.padding(16.dp)) {
            FloatingDebugButtonContent(pressed = false, modifier = Modifier.padding(8.dp))
            FloatingDebugButtonContent(pressed = true, modifier = Modifier.padding(8.dp))
        }
    }
}
