package com.sloy.sevibus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
fun CircularIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit
) {

    IconButton(
        onClick = onClick,
        modifier = modifier
            .clip(CircleShape)
            .background(SevTheme.colorScheme.outlineVariant),
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
    ) {
        CompositionLocalProvider(LocalContentColor provides SevTheme.colorScheme.onSurface) {
            content()
        }
    }

}
