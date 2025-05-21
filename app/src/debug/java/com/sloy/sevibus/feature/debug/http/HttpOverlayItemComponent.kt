package com.sloy.sevibus.feature.debug.http

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.ui.preview.ScreenPreview
import com.sloy.sevibus.ui.theme.Cyany700
import com.sloy.sevibus.ui.theme.Green500
import com.sloy.sevibus.ui.theme.Pink500

@Composable
fun HttpOverlayItemComponent(item: HttpOverlayItem, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = modifier
            .padding(vertical = 1.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(Color.White.copy(alpha = 0.8f)),
    ) {
        val statusColor = if (item.status == null) {
            Color.Gray
        } else if (item.cache == HttpOverlayItem.Cache.LOCAL) {
            Cyany700
        } else if (item.status < 300) {
            Green500
        } else if (item.status == 304) {
            Green500
        } else {
            Pink500
        }
        val cacheLabel: String? = when (item.cache) {
            HttpOverlayItem.Cache.LOCAL -> "(local)"
            HttpOverlayItem.Cache.NOT_MODIFIED -> "(not modified)"
            HttpOverlayItem.Cache.MISS -> null
            null -> null
        }
        val statusLabel: String? = item.status.let {
            if (it == 999) {
                item.error ?: "Error"
            } else {
                it?.toString()
            }
        }
        Text(item.endpoint.replace("/dev/api", ""), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 4.dp))
        Spacer(Modifier.size(8.dp))
        Text(
            listOf(item.method, statusLabel, cacheLabel).filterNotNull().joinToString(" "),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(MaterialTheme.shapes.extraLarge)
                .background(statusColor)
                .padding(horizontal = 6.dp)
        )
    }
}


@Preview
@Composable
private fun Preview() {
    ScreenPreview {
        Column(Modifier.background(Color.Black), horizontalAlignment = Alignment.End) {
            HttpOverlayItemComponent(HttpOverlayItem(method = "GET"))
            HttpOverlayItemComponent(HttpOverlayItem(method = "GET", status = 200))
            HttpOverlayItemComponent(HttpOverlayItem(method = "GET", status = 200, cache = HttpOverlayItem.Cache.LOCAL))
            HttpOverlayItemComponent(HttpOverlayItem(method = "GET", status = 304, cache = HttpOverlayItem.Cache.NOT_MODIFIED))
            HttpOverlayItemComponent(HttpOverlayItem(method = "GET", status = 500))
            HttpOverlayItemComponent(HttpOverlayItem(method = "POST", status = null))
            HttpOverlayItemComponent(HttpOverlayItem(method = "PUT", status = 999))
            HttpOverlayItemComponent(HttpOverlayItem(method = "PUT", status = 999, error = "Canceled"))
        }
    }
}
