package com.sloy.sevibus.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CustomFab(
    onClick: () -> Unit,
    color: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    content: @Composable() (BoxScope.() -> Unit),
) {
    Surface(
        onClick = onClick,
        color = color,
        contentColor = contentColor,
        shadowElevation = 8.dp,
        modifier = modifier.semantics { role = Role.Button },
        shape = CircleShape,
    ) {
        Box(Modifier.defaultMinSize(size, size), contentAlignment = Alignment.Center, content = content)
    }
}
