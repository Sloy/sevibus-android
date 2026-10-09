package com.sloy.debugmenu.events

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

enum class EventType(
    val color: Color,
    val onDark: Color,
    val ink: Color,
    val inkOnDark: Color,
    val tintAlpha: Float,
    val tintAlphaOnDark: Float,
) {
    CLICK(Color(0xFF4CAF50), Color(0xFF7BD67F), Color(0xFF2E7D32), Color(0xFF7BD67F), 0.16f, 0.18f),
    VIEW(Color(0xFF3F51B5), Color(0xFF8C9BFF), Color(0xFF2F3D8F), Color(0xFFB4BEFF), 0.16f, 0.20f),
    OTHER(Color(0xFFFFA726), Color(0xFFFFB547), Color(0xFF8A4B00), Color(0xFFFFC977), 0.18f, 0.18f);

    companion object {
        fun of(eventName: String): EventType = when {
            eventName.endsWith("Clicked", ignoreCase = true) -> CLICK
            eventName.endsWith("Viewed", ignoreCase = true) -> VIEW
            else -> OTHER
        }
    }
}

@Immutable
internal data class EventColors(val accent: Color, val tint: Color, val ink: Color)

internal fun EventType.colors(dark: Boolean): EventColors =
    if (dark) EventColors(onDark, onDark.copy(alpha = tintAlphaOnDark), inkOnDark)
    else EventColors(color, color.copy(alpha = tintAlpha), ink)

@Composable
@ReadOnlyComposable
internal fun isDarkSurface(): Boolean = MaterialTheme.colorScheme.surface.luminance() < 0.5f

@Composable
@ReadOnlyComposable
internal fun EventType.colors(): EventColors = colors(isDarkSurface())

internal fun screenBandColor(dark: Boolean): Color =
    if (dark) EventType.VIEW.onDark.copy(alpha = 0.07f) else EventType.VIEW.color.copy(alpha = 0.06f)
