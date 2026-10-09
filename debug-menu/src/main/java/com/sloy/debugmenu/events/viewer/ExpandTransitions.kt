package com.sloy.debugmenu.events.viewer

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.TransformOrigin

internal const val EXPAND_MILLIS = 250
private const val POP_SCALE = 0.92f
private val TopCenter = TransformOrigin(0.5f, 0f)

internal val DetailsEnter: EnterTransition =
    expandVertically(tween(EXPAND_MILLIS, easing = FastOutSlowInEasing), expandFrom = Alignment.Top) +
        scaleIn(tween(EXPAND_MILLIS, easing = FastOutSlowInEasing), initialScale = POP_SCALE, transformOrigin = TopCenter) +
        fadeIn(tween(EXPAND_MILLIS - 50, delayMillis = 50))

internal val DetailsExit: ExitTransition =
    shrinkVertically(tween(EXPAND_MILLIS, easing = FastOutSlowInEasing), shrinkTowards = Alignment.Top) +
        scaleOut(tween(EXPAND_MILLIS, easing = FastOutSlowInEasing), targetScale = POP_SCALE, transformOrigin = TopCenter) +
        fadeOut(tween(EXPAND_MILLIS / 2))

internal val InlineEnter: EnterTransition =
    expandVertically(tween(EXPAND_MILLIS, easing = FastOutSlowInEasing), expandFrom = Alignment.Top) + fadeIn(tween(EXPAND_MILLIS))

internal val InlineExit: ExitTransition =
    shrinkVertically(tween(EXPAND_MILLIS, easing = FastOutSlowInEasing), shrinkTowards = Alignment.Top) + fadeOut(tween(EXPAND_MILLIS / 2))
