package com.sloy.sevibus.feature.debug.tracking.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloy.debugmenu.overlay.OverlayItem
import com.sloy.sevibus.infrastructure.analytics.SevEvent
import com.sloy.sevibus.infrastructure.analytics.events.Clicks
import com.sloy.sevibus.infrastructure.analytics.events.Events
import com.sloy.sevibus.infrastructure.analytics.events.Screens
import com.sloy.sevibus.ui.theme.SevTheme
import java.util.UUID

data class TrackingOverlayItem(
    val event: SevEvent,
    override val id: String = UUID.randomUUID().toString(),
) : OverlayItem {
    override val autoHide: Boolean = true

    @Composable
    @Suppress("MagicNumber")
    override fun Content(modifier: Modifier) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .padding(vertical = 1.dp, horizontal = 2.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(COLOR_BACKGROUND_GREY).copy(alpha = 0.8f))
                .padding(start = 2.dp),
        ) {

            Text(
                event.name,
                style = SevTheme.typography.bodyExtraSmall,
                overflow = TextOverflow.StartEllipsis, maxLines = 1,
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .weight(1f, fill = false),
            )

            val (icon, color) = when {
                event.name.endsWith("Clicked") -> {
                    Icons.Default.TouchApp to Color(COLOR_ICON_GREEN)
                }

                event.name.endsWith("Viewed") -> {
                    Icons.Default.RemoveRedEye to Color(COLOR_ICON_BLUE)
                }

                else -> {
                    Icons.Default.Bolt to Color(COLOR_ICON_ORANGE)
                }
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .size(16.dp)
                    .background(color, shape = CircleShape)
                    .padding(2.dp)
            )
        }
    }
}

private const val COLOR_ICON_GREEN = 0xFF4CAF50
private const val COLOR_ICON_BLUE = 0xFF3F51B5
private const val COLOR_ICON_ORANGE = 0xFFFFA726
private const val COLOR_BACKGROUND_GREY = 0xFFEEEEEE

@Preview(widthDp = 360)
@Composable
private fun Preview() {
    Column(Modifier.background(Color.White), horizontalAlignment = Alignment.End) {
        listOf(
            Screens.LinesViewed,
            Clicks.AddFavoriteClicked(42),
            Events.AppStarted
        ).forEach {
            TrackingOverlayItem(it).Content(Modifier)
        }
    }
}
