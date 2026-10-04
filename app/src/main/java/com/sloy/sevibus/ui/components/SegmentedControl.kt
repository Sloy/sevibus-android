package com.sloy.sevibus.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.ui.icons.Home
import com.sloy.sevibus.ui.icons.SevIcons
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
fun SegmentedControl(options: List<String>, selectedIndex: Int, modifier: Modifier = Modifier, onOptionSelected: (Int) -> Unit = {}) {
    SegmentedControl(
        size = options.size,
        content = { index ->
            Text(options[index], style = SevTheme.typography.bodySmallBold, textAlign = TextAlign.Center)
        },
        selectedIndex,
        modifier,
        onOptionSelected
    )
}

@Composable
fun SegmentedControl(
    size: Int,
    content: @Composable (index: Int) -> Unit,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    onOptionSelected: (Int) -> Unit = {}
) {


    var pillWidth by remember { mutableStateOf(0f) }
    val pillTranslationX by animateFloatAsState(
        targetValue = pillWidth * selectedIndex,
        animationSpec = tween(durationMillis = 200),
        label = "PillTranslationX"
    )

    Box(
        modifier
            .clip(SevTheme.shapes.large)
            .background(SevTheme.colorScheme.surface)
            .height(32.dp)
            .fillMaxWidth()
    ) {
        // Pill
        Box(
            Modifier
                .onGloballyPositioned {
                    pillWidth = it.size.width.toFloat()
                }
                .graphicsLayer {
                    translationX = pillTranslationX
                }
                .fillMaxWidth(1f / size)
                .clip(SevTheme.shapes.large)
                .background(SevTheme.colorScheme.background)
                .border(1.dp, Color(0xFFEFEFEF), SevTheme.shapes.large)
                .heightIn(min = 32.dp)
        )
        // Tabs
        Row(
            Modifier
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(size) { i ->
                Box(
                    contentAlignment = Alignment.Center, modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(SevTheme.shapes.large)
                            .clickable(
                                onClick = { onOptionSelected(i) }, role = Role.Button,
                                interactionSource = null,
                                indication = null
                            )
                ) {
                    val color = if (i == selectedIndex) SevTheme.colorScheme.onSurface else SevTheme.colorScheme.onSurfaceVariant
                    CompositionLocalProvider(LocalContentColor provides color) {
                        content(i)
                    }
                }
            }
        }
    }

}


@Preview
@Composable
internal fun SegmentedControlPreview() {
    SevTheme {
        SegmentedControl(options = listOf("First", "Second", "Third"), selectedIndex = 0)
    }
}

@Preview
@Composable
internal fun SegmentedControlCustomPreview() {
    SevTheme {
        SegmentedControl(
            size = 3,
            content = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(SevIcons.Home, contentDescription = null)
                    Text("Item")
                }
            }, selectedIndex = 0
        )
    }
}
