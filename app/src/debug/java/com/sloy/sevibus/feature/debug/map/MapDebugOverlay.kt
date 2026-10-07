package com.sloy.sevibus.feature.debug.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.maps.android.compose.CameraPositionState
import com.sloy.debugmenu.overlay.OverlayPill
import com.sloy.debugmenu.overlay.OverlayPillText
import com.sloy.sevibus.feature.debug.MapDebugOptions
import com.sloy.sevibus.feature.map.MapScreenState
import org.koin.compose.koinInject
import java.util.Locale

@Composable
fun rememberMapDebugOptions(): MapDebugOptions {
    if (LocalInspectionMode.current) return MapDebugOptions()
    val dataSource = koinInject<MapDebugModuleDataSource>()
    val state by dataSource.observeCurrentState().collectAsStateWithLifecycle()
    return state.toOptions()
}

/**
 * Debug drawings over the map. [contentPadding] is the map padding, so the overlay matches what the camera sees.
 */
@Composable
fun MapDebugOverlay(
    options: MapDebugOptions,
    state: MapScreenState,
    cameraPositionState: CameraPositionState,
    contentPadding: PaddingValues,
    fitPadding: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .fillMaxSize()
            .padding(contentPadding)
            .clearAndSetSemantics {}
    ) {
        if (options.showVisibleArea) {
            VisibleAreaGuide(fitPadding)
        }
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(4.dp)
        ) {
            if (options.showMapState) {
                DebugChip(state::class.simpleName.orEmpty())
            }
            if (options.showCamera) {
                val position = cameraPositionState.position
                DebugChip(
                    String.format(
                        Locale.US, "zoom %.2f · %.5f, %.5f",
                        position.zoom, position.target.latitude, position.target.longitude,
                    )
                )
            }
        }
    }
}

@Composable
private fun DebugChip(text: String) {
    OverlayPill {
        Text(text, style = MaterialTheme.typography.labelSmall, color = OverlayPillText, maxLines = 1)
    }
}

@Composable
private fun VisibleAreaGuide(fitPadding: Dp) {
    Canvas(Modifier.fillMaxSize()) {
        val inset = fitPadding.toPx()
        val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
        drawRect(VisibleAreaColor, style = Stroke(width = 2.dp.toPx()))
        drawRect(
            FitAreaColor,
            topLeft = Offset(inset, inset),
            size = size.copy(width = size.width - inset * 2, height = size.height - inset * 2),
            style = Stroke(width = 1.dp.toPx(), pathEffect = dash),
        )
        drawCrosshair(center, VisibleAreaColor)
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
