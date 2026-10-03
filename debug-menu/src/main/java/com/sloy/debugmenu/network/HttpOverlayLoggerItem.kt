package com.sloy.debugmenu.network

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloy.debugmenu.overlay.OverlayLoggerItem
import com.sloy.debugmenu.overlay.OverlayPill
import com.sloy.debugmenu.overlay.OverlayPillText

data class HttpOverlayLoggerItem(
    val method: String,
    val endpoint: String,
    override val id: String,
    val status: Int? = null,
    val error: String? = null,
    val cache: Cache? = null,
) : OverlayLoggerItem {

    override val autoHide: Boolean
        get() = status != null

    enum class Cache { LOCAL, NOT_MODIFIED, MISS }

    @Composable
    override fun Content(modifier: Modifier) {
        OverlayPill(modifier) {
            Text(
                redactPath(endpoint),
                style = MaterialTheme.typography.labelSmall,
                color = OverlayPillText,
                maxLines = 1,
                overflow = TextOverflow.StartEllipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            Spacer(Modifier.width(6.dp))
            val (badgeColor, badgeTextColor) = badgeColors()
            Text(
                badgeLabel(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = badgeTextColor,
                maxLines = 1,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeColor)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
    }

    companion object {
        const val STATUS_IO_EXCEPTION = 999
    }
}

internal fun HttpOverlayLoggerItem.badgeLabel(): String = when (status) {
    null -> method
    HttpOverlayLoggerItem.STATUS_IO_EXCEPTION -> (error ?: "Error").take(MAX_ERROR_LENGTH).trim()
    else -> status.toString()
}

private fun HttpOverlayLoggerItem.badgeColors(): Pair<Color, Color> = when {
    status == null -> Color(0xFF888888) to Color.White
    cache == HttpOverlayLoggerItem.Cache.LOCAL -> Color(0xFF00BCD4) to Color.Black
    status < 300 || status == 304 -> Color(0xFF4CAF50) to Color.White
    else -> Color(0xFFE91E63) to Color.White
}

internal fun redactPath(path: String): String = path.split("/").joinToString("/") { segment ->
    when {
        UUID_REGEX.matches(segment) -> "{id}"
        segment.length > MAX_SEGMENT_LENGTH -> segment.take(3) + "…" + segment.takeLast(3)
        else -> segment
    }
}

private val UUID_REGEX = Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")
private const val MAX_SEGMENT_LENGTH = 40
private const val MAX_ERROR_LENGTH = 17

@Preview(widthDp = 320)
@Composable
private fun HttpOverlayLoggerItemPreview() {
    Column(Modifier.background(Color.DarkGray), horizontalAlignment = Alignment.End) {
        HttpOverlayLoggerItem("GET", "/api/stops/123", id = "1").Content(Modifier)
        HttpOverlayLoggerItem("GET", "/api/lines", id = "2", status = 200, cache = HttpOverlayLoggerItem.Cache.LOCAL).Content(Modifier)
        HttpOverlayLoggerItem("POST", "/api/favorites", id = "3", status = 201).Content(Modifier)
        HttpOverlayLoggerItem("GET", "/api/cards", id = "4", status = 500).Content(Modifier)
        HttpOverlayLoggerItem("GET", "/api/arrivals", id = "5", status = 999, error = "timeout").Content(Modifier)
    }
}
