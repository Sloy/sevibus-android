package com.sloy.debugmenu.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class EventType(val color: Color) {
    CLICK(Color(0xFF4CAF50)),
    VIEW(Color(0xFF3F51B5)),
    OTHER(Color(0xFFFFA726));

    companion object {
        fun of(eventName: String): EventType = when {
            eventName.endsWith("Clicked", ignoreCase = true) -> CLICK
            eventName.endsWith("Viewed", ignoreCase = true) -> VIEW
            else -> OTHER
        }
    }
}

@Composable
internal fun EventTypeIcon(type: EventType, modifier: Modifier = Modifier) {
    val icon = when (type) {
        EventType.CLICK -> Icons.Default.TouchApp
        EventType.VIEW -> Icons.Default.Visibility
        EventType.OTHER -> Icons.Default.Bolt
    }
    Icon(
        icon,
        contentDescription = null,
        tint = Color.White,
        modifier = modifier
            .size(16.dp)
            .background(type.color, CircleShape)
            .padding(2.dp),
    )
}
