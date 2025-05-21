package com.sloy.sevibus.ui.wiggle

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.random.Random


@Composable
fun Modifier.wiggle(
    duration: Int = 150, // Duration of the animation in milliseconds
    displacement: Dp = 2.dp, // Displacement for jitter effect
    degreesRotation: Float = 4f // Rotation in degrees
): Modifier {

    val directionX = remember { if (Random.nextBoolean()) 1 else -1 }
    val directionY = remember { if (Random.nextBoolean()) 1 else -1 }
    val directionRotation = remember { if (Random.nextBoolean()) 1 else -1 }

    // Infinite transition for animations
    val infiniteTransition = rememberInfiniteTransition()

    // Animate position displacement
    val offsetX by infiniteTransition.animateFloat(
        initialValue = -displacement.value * directionX,
        targetValue = displacement.value * directionX,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val offsetY by infiniteTransition.animateFloat(
        initialValue = -displacement.value * directionY,
        targetValue = displacement.value * directionY,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Animate rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = -degreesRotation * directionRotation,
        targetValue = degreesRotation * directionRotation,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    return this.graphicsLayer {
        translationX = offsetX
        translationY = offsetY
        rotationZ = rotation
    }
}
