package com.sloy.sevibus.ui.shimmer

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Shimmer(modifier: Modifier = Modifier) {
    Box(modifier.shimmerLoadingAnimation())
}

fun Modifier.shimmerLoadingAnimation(
    isLoadingCompleted: Boolean = false,
    isLightModeActive: Boolean = true,
    widthOfShadowBrush: Int = 500,
    angleOfAxisY: Float = 270f,
    durationMillis: Int = 2000,
): Modifier {
    if (isLoadingCompleted) {
        return this
    } else {
        return composed {
            val shimmerColors = ShimmerAnimationData(isLightMode = isLightModeActive).getColours()

            val transition = rememberInfiniteTransition(label = "")

            val translateAnimation = transition.animateFloat(
                initialValue = 0f,
                targetValue = (durationMillis + widthOfShadowBrush).toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = durationMillis,
                        easing = LinearEasing,
                    ),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "Shimmer loading animation",
            )

            this
                .clip(RoundedCornerShape(8.dp))
                //.background(Color.Gray)
                .background(
                    brush = Brush.linearGradient(
                        colors = shimmerColors,
                        start = Offset(x = translateAnimation.value - widthOfShadowBrush, y = 0.0f),
                        end = Offset(x = translateAnimation.value, y = angleOfAxisY),
                    ),
                )
        }
    }
}

data class ShimmerAnimationData(
    private val isLightMode: Boolean
) {
    fun getColours(): List<Color> {
        return if (isLightMode) {
            val color = Color.LightGray

            listOf(
                color.copy(alpha = 0.3f),
                color.copy(alpha = 0.5f),
                color.copy(alpha = 1.0f),
                color.copy(alpha = 0.5f),
                color.copy(alpha = 0.3f),
            )
        } else {
            val color = Color.DarkGray

            listOf(
                color.copy(alpha = 0.0f),
                color.copy(alpha = 0.3f),
                color.copy(alpha = 0.5f),
                color.copy(alpha = 0.3f),
                color.copy(alpha = 0.0f),
            )
        }
    }
}
