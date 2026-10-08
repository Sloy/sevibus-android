package com.sloy.sevibus.ui.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.TransformOrigin

object SevMotion {

    val duration = Duration
    val easing = SevEasing
    val anim = Anim

    object Duration {
        const val d50 = 50
        const val d100 = 100
        const val d150 = 150
        const val d200 = 200
        const val d250 = 250
        const val d300 = 300
        const val d350 = 350
        const val d400 = 400
        const val d450 = 450
        const val d500 = 500
        const val d550 = 550
        const val d600 = 600
    }

    object SevEasing {
        val ease: Easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)
        val standard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
        val inOut: Easing = CubicBezierEasing(0.3f, 0f, 0.1f, 1f)
        val accelerate: Easing = CubicBezierEasing(0.4f, 0f, 1f, 1f)
        val overshoot: Easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)
    }

    object Anim {
        fun fadeAndScaleIn(
            expandDuration: Int,
            fadeDuration: Int,
            scaleDuration: Int,
            initialScale: Float,
            delay: Int = 0,
            contentDelay: Int = 0,
            scaleEasing: Easing = SevEasing.standard,
            transformOrigin: TransformOrigin = TransformOrigin.Center,
        ): EnterTransition =
            expandVertically(tween(expandDuration, delayMillis = delay, easing = SevEasing.standard), expandFrom = Alignment.Top) +
                fadeIn(tween(fadeDuration, delayMillis = delay + contentDelay, easing = SevEasing.ease)) +
                scaleIn(
                    tween(scaleDuration, delayMillis = delay + contentDelay, easing = scaleEasing),
                    initialScale = initialScale,
                    transformOrigin = transformOrigin,
                )

        fun fadeAndScaleOut(
            fadeDuration: Int,
            scaleDuration: Int,
            shrinkDuration: Int,
            targetScale: Float,
            shrinkDelay: Int = 0,
            scaleEasing: Easing = SevEasing.ease,
            transformOrigin: TransformOrigin = TransformOrigin.Center,
        ): ExitTransition =
            fadeOut(tween(fadeDuration, easing = SevEasing.ease)) +
                scaleOut(tween(scaleDuration, easing = scaleEasing), targetScale = targetScale, transformOrigin = transformOrigin) +
                shrinkVertically(tween(shrinkDuration, delayMillis = shrinkDelay, easing = SevEasing.inOut), shrinkTowards = Alignment.Top)
    }
}
