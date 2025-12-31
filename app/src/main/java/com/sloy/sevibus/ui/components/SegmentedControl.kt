package com.sloy.sevibus.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.sloy.sevibus.ui.theme.SevTheme


@Composable
fun SegmentedControl(options: List<String>, selectedIndex: Int, modifier: Modifier = Modifier, onOptionSelected: (Int) -> Unit = {}) {

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
                .fillMaxWidth(1f / options.size)
                .clip(SevTheme.shapes.large)
                .background(SevTheme.colorScheme.background)
                .border(1.dp, Color(0xFFEFEFEF), SevTheme.shapes.large)
                .height(32.dp)
        )
        // Tabs
        Row(
            Modifier
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            options.forEachIndexed { index, option ->
                Box(
                    contentAlignment = Alignment.Center, modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(SevTheme.shapes.large)
                            .clickable(
                                onClick = { onOptionSelected(index) }, role = Role.Button,
                                interactionSource = null,
                                indication = null
                            )
                ) {
                    Text(option, style = SevTheme.typography.bodySmallBold, textAlign = TextAlign.Center)
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
