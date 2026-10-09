package com.sloy.debugmenu.events.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sloy.debugmenu.base.DebugPreviewTheme
import com.sloy.debugmenu.base.ScreenshotSuite
import com.sloy.debugmenu.base.ScreenshotTest
import com.sloy.debugmenu.events.EventText
import com.sloy.debugmenu.events.toClockTimeMillis

@Composable
internal fun PropertyBox(properties: Map<String, String>, timestampMillis: Long?, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val keys = properties.keys + listOfNotNull("timestamp".takeIf { timestampMillis != null })
    val keyWidth = with(LocalDensity.current) { keys.maxOfOrNull { measurer.measure(it, EventText.Mono12).size.width }?.toDp() ?: 0.dp }
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        properties.forEach { (key, value) -> PropertyLine(key, value, keyWidth, MaterialTheme.colorScheme.onSurface, EventText.Mono12Medium) }
        if (timestampMillis != null) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            PropertyLine("timestamp", timestampMillis.toClockTimeMillis(), keyWidth, MaterialTheme.colorScheme.onSurfaceVariant, EventText.Mono12)
        }
    }
}

@Composable
private fun PropertyLine(key: String, value: String, keyWidth: Dp, valueColor: Color, valueStyle: TextStyle) {
    Row {
        Text(key, style = EventText.Mono12, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(keyWidth))
        Spacer(Modifier.width(16.dp))
        Text(value, style = valueStyle, color = valueColor, modifier = Modifier.weight(1f))
    }
}

@ScreenshotTest(ScreenshotSuite.Components)
@PreviewLightDark
@Composable
internal fun PropertyBoxPreview() {
    DebugPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            val event = ViewerSampleData.events.first { it.id == ViewerSampleData.expandedEventId }
            PropertyBox(event.properties, event.timestampMillis, Modifier.padding(16.dp))
        }
    }
}
