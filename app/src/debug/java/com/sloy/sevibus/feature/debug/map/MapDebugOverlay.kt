package com.sloy.sevibus.feature.debug.map

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.maps.android.compose.CameraPositionState
import com.sloy.debugmenu.overlay.OverlayPill
import com.sloy.sevibus.feature.debug.MapDebugOptions
import com.sloy.sevibus.feature.map.MapScreenState
import org.koin.compose.koinInject
import java.util.Locale
import kotlin.math.floor

@Composable
fun rememberMapDebugOptions(): MapDebugOptions {
    if (LocalInspectionMode.current) return MapDebugOptions()
    val dataSource = koinInject<MapDebugModuleDataSource>()
    val state by dataSource.observeCurrentState().collectAsStateWithLifecycle()
    return state.toOptions()
}

/**
 * Debug drawings over the map. [contentPadding] is the map padding and [fitArea] the area, from the map edges, where the
 * camera fits lines and stops.
 */
@Composable
fun MapDebugOverlay(
    options: MapDebugOptions,
    state: MapScreenState,
    cameraPositionState: CameraPositionState,
    contentPadding: PaddingValues,
    fitArea: PaddingValues,
    modifier: Modifier = Modifier,
) {
    if (options.showCamera) {
        ZoomLevelTicks(cameraPositionState)
    }
    if (options.showVisibleArea) {
        VisibleAreaGuide(contentPadding, fitArea, modifier.clearAndSetSemantics {})
    }
    Box(
        modifier
            .fillMaxSize()
            .padding(contentPadding)
            .clearAndSetSemantics {}
    ) {
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (options.showMapState) {
                DebugChip(state::class.simpleName.orEmpty())
            }
            if (options.showCamera) {
                val position = cameraPositionState.position
                DebugChip(String.format(Locale.US, "zoom %.2f", position.zoom))
                DebugChip(String.format(Locale.US, "%.5f, %.5f", position.target.latitude, position.target.longitude))
            }
        }
    }
}

/**
 * Ticks every time the zoom crosses a whole level, so zoom thresholds can be felt while pinching.
 */
@Composable
private fun ZoomLevelTicks(cameraPositionState: CameraPositionState) {
    val view = LocalView.current
    val wholeZoom = floor(cameraPositionState.position.zoom).toInt()
    var previousWholeZoom by remember { mutableIntStateOf(wholeZoom) }
    LaunchedEffect(wholeZoom) {
        if (wholeZoom != previousWholeZoom) {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            previousWholeZoom = wholeZoom
        }
    }
}

@Composable
private fun DebugChip(text: String) {
    OverlayPill(background = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.8f)) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.inverseOnSurface, maxLines = 1)
    }
}

@Composable
private fun VisibleAreaGuide(contentPadding: PaddingValues, fitArea: PaddingValues, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        AreaOutline(contentPadding, VisibleAreaColor, dashed = false)
        AreaOutline(fitArea, FitAreaColor, dashed = true)
    }
}

@Composable
private fun AreaOutline(padding: PaddingValues, color: Color, dashed: Boolean) {
    Canvas(
        Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        val pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(12f, 8f)) else null
        drawRect(color, style = Stroke(width = 2.dp.toPx(), pathEffect = pathEffect))
        drawCrosshair(center, color)
    }
}

private fun DrawScope.drawCrosshair(point: Offset, color: Color) {
    val arm = 12.dp.toPx()
    val stroke = 2.dp.toPx()
    drawLine(color, point.copy(x = point.x - arm), point.copy(x = point.x + arm), strokeWidth = stroke)
    drawLine(color, point.copy(y = point.y - arm), point.copy(y = point.y + arm), strokeWidth = stroke)
}

private val VisibleAreaColor = Color(0xFFE91E63)
private val FitAreaColor = Color(0xFF2196F3)
