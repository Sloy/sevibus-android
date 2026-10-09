package com.sloy.debugmenu.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloy.debugmenu.overlay.OverlayLoggerItem
import com.sloy.debugmenu.overlay.OverlayPill
import com.sloy.debugmenu.overlay.OverlayPillText

data class EventOverlayLoggerItem(val event: CapturedEvent) : OverlayLoggerItem {
    override val id: String
        get() = event.id
    override val autoHide: Boolean
        get() = true

    @Composable
    override fun Content(modifier: Modifier) {
        OverlayPill(modifier) {
            Text(
                event.name,
                style = MaterialTheme.typography.labelSmall,
                color = OverlayPillText,
                maxLines = 1,
                overflow = TextOverflow.StartEllipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            Spacer(Modifier.width(4.dp))
            EventTypeIcon(EventType.of(event.name))
        }
    }
}

@Preview(widthDp = 320)
@Composable
private fun EventOverlayLoggerItemPreview() {
    Column(Modifier.background(Color.DarkGray), horizontalAlignment = Alignment.End) {
        listOf("Lines Viewed", "Add Favorite Clicked", "App Started").forEach { name ->
            EventOverlayLoggerItem(CapturedEvent(name = name, timestampMillis = 0, id = name)).Content(Modifier)
        }
    }
}
