package com.sloy.sevibus.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.ui.icons.Home
import com.sloy.sevibus.ui.icons.SevIcons
import com.sloy.sevibus.ui.preview.ScreenshotSuite
import com.sloy.sevibus.ui.preview.ScreenshotTest
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
fun SegmentedControl(options: List<String>, selectedIndex: Int, modifier: Modifier = Modifier, onOptionSelected: (Int) -> Unit = {}) {
    SegmentedControl(
        options.size, selectedIndex, modifier, onOptionSelected
    ) { index ->
        Text(options[index], style = SevTheme.typography.bodySmallBold, textAlign = TextAlign.Center)
    }
}

@Composable
fun SegmentedControl(
    segmentCount: Int,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    onOptionSelected: (Int) -> Unit,
    segments: @Composable (index: Int) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .requiredHeight(48.dp)
            .clip(RoundedCornerShape(9999.dp))
            .background(SevTheme.colorScheme.surfaceVariant),
    ) {
        if (segmentCount > 0) {
            SelectedSegmentHighlighter(
                selectedIndex = selectedIndex, segmentCount = segmentCount
            )
        }
        @Suppress("COMPOSE_APPLIER_CALL_MISMATCH") Layout(
            modifier = Modifier.fillMaxSize(), content = {
                repeat(segmentCount) { index ->
                    val color = if (index == selectedIndex) SevTheme.colorScheme.onSurface else SevTheme.colorScheme.onSurfaceVariant
                    CompositionLocalProvider(LocalContentColor provides color) {
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .clickable(onClick = { onOptionSelected(index) })
                                .semantics {
                                    role = Role.Tab
                                    selected = index == selectedIndex
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            segments(index)
                        }
                    }
                }
            }) { measurables, constraints ->
            val segmentWidth = constraints.maxWidth / measurables.size
            val segmentConstraints = constraints.copy(
                minWidth = segmentWidth, maxWidth = segmentWidth
            )

            val measuredSegments = measurables.map { it.measure(segmentConstraints) }

            layout(constraints.maxWidth, constraints.maxHeight) {
                var xOffset = 0
                measuredSegments.forEach { placeable ->
                    placeable.placeRelative(xOffset, 0)
                    xOffset += segmentWidth
                }
            }
        }
    }
}

@Composable
private fun SelectedSegmentHighlighter(
    selectedIndex: Int, segmentCount: Int
) {
    val animatedOffset by animateFloatAsState(
        targetValue = selectedIndex / segmentCount.toFloat(),
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "selectedIndexAnimation"
    )
    Row(modifier = Modifier.fillMaxSize()) {
        if (animatedOffset > 0f) {
            Box(
                modifier = Modifier
                    .weight(animatedOffset)
                    .fillMaxHeight()
            )
        }
        Box(
            modifier = Modifier
                .weight(1f / segmentCount)
                .fillMaxSize()
                .fillMaxHeight()
                .padding(2.dp)
                .clip(shape = RoundedCornerShape(9999.dp))
                .background(SevTheme.colorScheme.background)

        )
        val rightWeight = 1f - animatedOffset - (1f / segmentCount)
        if (rightWeight > 0f) {
            Box(
                modifier = Modifier
                    .weight(rightWeight)
                    .fillMaxHeight()
            )
        }
    }
}


@ScreenshotTest(ScreenshotSuite.Components)
@Preview
@Composable
internal fun SegmentedControlPreview() {
    SevTheme {
        SevTheme {
            var selectedIndex by remember { mutableIntStateOf(0) }
            Box(
                Modifier
                    .background(SevTheme.colorScheme.background)
                    .padding(32.dp)
            ) {
                SegmentedControl(options = listOf("First", "Second", "Third"), selectedIndex = 0)
            }
        }
    }
}


@Preview
@Composable
internal fun SegmentedControlPulsePreview() {
    SevTheme {
        var selectedIndex by remember { mutableIntStateOf(0) }
        Box(
            Modifier
                .background(SevTheme.colorScheme.background)
                .padding(32.dp)
        ) {
            SegmentedControl(
                segmentCount = 3,
                selectedIndex = selectedIndex,
                onOptionSelected = { selectedIndex = it },
            ) { index ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(SevIcons.Home, contentDescription = null)
                    Text("Item $index")
                }
            }
        }
    }
}
