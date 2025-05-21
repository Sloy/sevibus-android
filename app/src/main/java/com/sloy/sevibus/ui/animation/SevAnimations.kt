package com.sloy.sevibus.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationResult
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween


suspend fun Animatable<Float, AnimationVector1D>.animateShake() = this.animateTo(
    targetValue = 0f,
    animationSpec = keyframes {
        durationMillis = 300
        for (i in 0..10) {
            val direction = if (i % 2 == 0) 1 else -1
            (direction * 10).toFloat() at (i * 50)
        }
    }
)

suspend fun Animatable<Float, AnimationVector1D>.animatePulse() {
    this.animateTo(
        targetValue = 1.1f, // Scale up
        animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing)
    )
    this.animateTo(
        targetValue = 1f, // Scale back to normal
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
    )
}
