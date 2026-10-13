package com.sloy.debugmenu.base

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp

/**
 * Full-width pill shaped single choice control. The selected option is a raised pill sliding between positions.
 */
@Composable
fun PillSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        val optionWidth = maxWidth / options.size
        val thumbOffset by animateDpAsState(
            targetValue = optionWidth * selectedIndex.coerceIn(0, options.lastIndex),
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "pillThumbOffset",
        )
        Box(
            Modifier
                .offset(x = thumbOffset)
                .width(optionWidth)
                .fillMaxHeight()
                .shadow(1.dp, CircleShape)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
        )
        Row(Modifier.fillMaxSize().selectableGroup()) {
            options.forEachIndexed { index, label ->
                val isSelected = index == selectedIndex
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .selectable(
                            selected = isSelected,
                            interactionSource = null,
                            indication = ripple(color = MaterialTheme.colorScheme.surface),
                            role = Role.Tab,
                            onClick = { onSelected(index) },
                        ),
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
            }
        }
    }
}

@ScreenshotTest(ScreenshotSuite.Components)
@PreviewLightDark
@Composable
internal fun PillSegmentedControlTimelinePreview() {
    DebugPreviewTheme {
        PillSegmentedControl(listOf("Timeline", "Journey"), selectedIndex = 0, onSelected = {}, modifier = Modifier.padding(16.dp))
    }
}
