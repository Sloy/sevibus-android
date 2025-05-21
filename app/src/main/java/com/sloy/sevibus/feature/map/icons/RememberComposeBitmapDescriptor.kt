package com.sloy.sevibus.feature.map.icons

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalView
import androidx.core.graphics.applyCanvas
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.GoogleMapComposable

/**
 * This is a copy from Google Maps Compose SDK. This function is internal, intended to be used by [MarkerComposable].
 *
 * I'm copying it to my project due to this issue: https://github.com/googlemaps/android-maps-compose/issues/685
 * TODO: When the issue is resolved, this function can be removed.
 */
@Composable
@GoogleMapComposable
internal fun rememberComposeBitmapDescriptor(
    vararg keys: Any,
    content: @Composable () -> Unit,
): BitmapDescriptor {
    val parent = LocalView.current as ViewGroup
    val compositionContext = rememberCompositionContext()
    val currentContent by rememberUpdatedState(content)

    return remember(parent, compositionContext, currentContent, *keys) {
        renderComposableToBitmapDescriptor(parent, compositionContext, currentContent)
    }
}

private val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)

private fun renderComposableToBitmapDescriptor(
    parent: ViewGroup,
    compositionContext: CompositionContext,
    content: @Composable () -> Unit,
): BitmapDescriptor {
    val fakeCanvas = Canvas()
    val composeView =
        ComposeView(parent.context)
            .apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                setParentCompositionContext(compositionContext)
                setContent(content)
            }
            .also(parent::addView)

    composeView.draw(fakeCanvas)

    composeView.measure(measureSpec, measureSpec)

    if (composeView.measuredWidth == 0 || composeView.measuredHeight == 0) {
        throw IllegalStateException(
            "The ComposeView was measured to have a width or height of " +
                    "zero. Make sure that the content has a non-zero size."
        )
    }

    composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)

    val bitmap =
        Bitmap
            .createBitmap(
                composeView.measuredWidth,
                composeView.measuredHeight,
                Bitmap.Config.ARGB_8888,
            )

    bitmap.applyCanvas { composeView.draw(this) }

    parent.removeView(composeView)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}
