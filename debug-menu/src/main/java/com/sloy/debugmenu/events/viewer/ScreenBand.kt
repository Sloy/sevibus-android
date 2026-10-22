package com.sloy.debugmenu.events.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.sloy.debugmenu.base.DebugPreviewTheme
import com.sloy.debugmenu.base.ScreenshotSuite
import com.sloy.debugmenu.base.ScreenshotTest
import com.sloy.debugmenu.events.EventText
import com.sloy.debugmenu.events.EventType
import com.sloy.debugmenu.events.colors
import com.sloy.debugmenu.events.isDarkSurface
import com.sloy.debugmenu.events.screenBandColor
import com.sloy.debugmenu.events.toClockTime

private val BandShapeTop = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
private val BandShapeBottom = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)

@Composable
internal fun Modifier.screenBandBackground(): Modifier = padding(horizontal = 8.dp).background(screenBandColor(isDarkSurface()))

@Composable
internal fun ScreenBandHeader(item: BandHeaderItem, modifier: Modifier = Modifier) {
    val view = EventType.VIEW.colors()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(start = 8.dp, end = 8.dp, top = 8.dp)
            .fillMaxWidth()
            .background(screenBandColor(isDarkSurface()), BandShapeTop)
            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
    ) {
        Text(item.startMillis.toClockTime(), style = EventText.Mono11, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(60.dp))
        Box(Modifier.size(24.dp).background(view.tint, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.PhoneAndroid, contentDescription = null, tint = view.accent, modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(item.screen, style = MaterialTheme.typography.titleMedium, color = view.ink, modifier = Modifier.weight(1f))
        Text(
            item.duration ?: "now",
            style = EventText.Mono11Medium,
            color = view.ink,
            modifier = Modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp)).padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

@Composable
internal fun ScreenBandEnd(modifier: Modifier = Modifier) {
    Box(
        modifier
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            .fillMaxWidth()
            .height(12.dp)
            .background(screenBandColor(isDarkSurface()), BandShapeBottom)
    )
}

@ScreenshotTest(ScreenshotSuite.Components)
@PreviewLightDark
@Composable
internal fun ScreenBandHeaderResumedPreview() {
    DebugPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            ScreenBandHeader(BandHeaderItem("k", "For You (resumed)", startMillis = ViewerSampleData.startMillis + 145_300, duration = null))
        }
    }
}

@ScreenshotTest(ScreenshotSuite.Components)
@PreviewLightDark
@Composable
internal fun ScreenBandPreview() {
    DebugPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            val rows = listOf(
                ViewerSampleData.events.first { it.id == ViewerSampleData.id(16) } to "+2.75s",
                ViewerSampleData.events.first { it.id == ViewerSampleData.id(15) } to "+0.61s",
                ViewerSampleData.events.first { it.id == ViewerSampleData.id(14) } to "+0.02s",
            )
            Column {
                ScreenBandHeader(BandHeaderItem("k", "Stop Details", ViewerSampleData.startMillis + 9_336, "3.4s"))
                rows.forEach { (event, delta) ->
                    EventRow(event, delta, expanded = false, onToggle = {}, inBand = true, modifier = Modifier.screenBandBackground())
                }
                ScreenBandEnd()
            }
        }
    }
}
