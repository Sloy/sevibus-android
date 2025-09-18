package com.sloy.sevibus.feature.debug.network.overlay

import androidx.compose.foundation.background
import com.sloy.debugmenu.overlay.OverlayItem
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.ui.theme.Cyany700
import com.sloy.sevibus.ui.theme.Green500
import com.sloy.sevibus.ui.theme.Pink500
import java.util.UUID

data class HttpOverlayItem(
    val method: String,
    val endpoint: String,
    val status: Int? = null,
    val error: String? = null,
    val cache: Cache? = null,
    override val id: String = UUID.randomUUID().toString(),
) : OverlayItem {
    override val autoHide = true

    @Composable
    override fun Content(modifier: Modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = modifier
                .padding(vertical = 1.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(Color.White.copy(alpha = 0.8f)),
        ) {
            val statusColor = if (status == null) {
                Color.Gray
            } else if (cache == Cache.LOCAL) {
                Cyany700
            } else if (status < 300) {
                Green500
            } else if (status == 304) {
                Green500
            } else {
                Pink500
            }
            val cacheLabel: String? = when (cache) {
                Cache.LOCAL -> "(local)"
                Cache.NOT_MODIFIED -> "(not modified)"
                Cache.MISS -> null
                null -> null
            }
            val statusLabel: String? = status.let {
                if (it == 999) {
                    error ?: "Error"
                } else {
                    it?.toString()
                }
            }
            Text(endpoint.replace("/dev/api", ""), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 4.dp))
            Spacer(Modifier.size(8.dp))
            Text(
                listOf(method, statusLabel, cacheLabel).filterNotNull().joinToString(" "),
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

    enum class Cache {
        LOCAL,
        NOT_MODIFIED,
        MISS
    }
}

