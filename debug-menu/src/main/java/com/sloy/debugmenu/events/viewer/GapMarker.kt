package com.sloy.debugmenu.events.viewer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.sloy.debugmenu.base.DebugPreviewTheme
import com.sloy.debugmenu.base.ScreenshotSuite
import com.sloy.debugmenu.base.ScreenshotTest
import com.sloy.debugmenu.events.EventText

@Composable
internal fun GapMarker(label: String, modifier: Modifier = Modifier, inBand: Boolean = false) {
    val dash = MaterialTheme.colorScheme.outline
    Row(modifier.fillMaxWidth().height(32.dp), verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.width(if (inBand) 68.dp else 76.dp))
        Canvas(Modifier.width(24.dp).fillMaxHeight()) {
            drawLine(
                dash,
                Offset(size.width / 2, 0f),
                Offset(size.width / 2, size.height),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx())),
            )
        }
        Text(label, style = EventText.Mono11, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@ScreenshotTest(ScreenshotSuite.Components)
@PreviewLightDark
@Composable
internal fun GapMarkerBackgroundPreview() {
    DebugPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            GapMarker("· 2m 13s in background ·")
        }
    }
}
