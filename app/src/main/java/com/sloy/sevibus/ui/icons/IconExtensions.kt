package com.sloy.sevibus.ui.icons

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.scale
import com.sloy.sevibus.R


@Composable
fun ImageVector.asBitmap(tint: Color? = null, scaledSize: Int? = null): Bitmap {
    val painter = rememberVectorPainter(this)
    val drawScope = CanvasDrawScope()
    val size = painter.intrinsicSize
    val bitmap = ImageBitmap(size.width.toInt(), size.height.toInt())
    val canvas = androidx.compose.ui.graphics.Canvas(bitmap)

    drawScope.draw(
        density = Density(1f),
        layoutDirection = LayoutDirection.Ltr,
        canvas = canvas,
        size = size,
    ) {
        with(painter) {
            draw(
                size = painter.intrinsicSize,
                colorFilter = tint?.let { ColorFilter.tint(it) }
            )
        }
    }
    return bitmap.asAndroidBitmap().let {
        if (scaledSize == null) it else it.scale(scaledSize, scaledSize)
    }
}

@Composable
fun layeredBitmap(vararg icons: Pair<ImageVector, Color>, size: Int? = null): Bitmap {
    check(icons.isNotEmpty())
    val painter = rememberVectorPainter(icons.first().first)
    val vectorSize = painter.intrinsicSize
    val bitmap = ImageBitmap(vectorSize.width.toInt(), vectorSize.height.toInt())
    val canvas = androidx.compose.ui.graphics.Canvas(bitmap)

    CanvasDrawScope().draw(
        density = Density(1f),
        layoutDirection = LayoutDirection.Ltr,
        canvas = canvas,
        size = vectorSize,
    ) {
        icons.forEach { (icon, color) ->
            val painter = rememberVectorPainter(icon)
            with(painter) {
                draw(
                    size = painter.intrinsicSize,
                    colorFilter = ColorFilter.tint(color)
                )
            }
        }
    }

    return bitmap.asAndroidBitmap().let {
        if (size == null) it else it.scale(size, size)
    }
}

@Composable
fun selectedStopBitmap(color: Color): Bitmap {
    val backgroundPainter = painterResource(id = R.drawable.ic_stop_selected_background_40)
    val foregroundPainter = painterResource(id = R.drawable.ic_stop_selected_foreground_40)
    //val foregroundPainter = rememberVectorPainter(SevIcons.StopSelectedForeground)

    val size = backgroundPainter.intrinsicSize
    val bitmap = ImageBitmap(size.width.toInt(), size.height.toInt())
    val canvas = androidx.compose.ui.graphics.Canvas(bitmap)

    CanvasDrawScope().draw(
        density = Density(1f),
        layoutDirection = LayoutDirection.Ltr,
        canvas = canvas,
        size = size,
    ) {
        with(backgroundPainter) {
            draw(
                size = this.intrinsicSize,
            )
        }
        with(foregroundPainter) {
            draw(
                size = this.intrinsicSize,
                colorFilter = ColorFilter.tint(color)
            )
        }
    }
    return bitmap.asAndroidBitmap()
}

@Composable
fun busBitmap(color: Color): Bitmap {
    val backgroundPainter = painterResource(id = R.drawable.ic_bus_background_48)
    val foregroundPainter = painterResource(id = R.drawable.ic_bus_foreground_48)

    val size = backgroundPainter.intrinsicSize
    val bitmap = ImageBitmap(size.width.toInt(), size.height.toInt())
    val canvas = androidx.compose.ui.graphics.Canvas(bitmap)

    CanvasDrawScope().draw(
        density = Density(1f),
        layoutDirection = LayoutDirection.Ltr,
        canvas = canvas,
        size = size,
    ) {
        with(backgroundPainter) {
            draw(
                size = this.intrinsicSize,
            )
        }
        with(foregroundPainter) {
            draw(
                size = this.intrinsicSize,
                colorFilter = ColorFilter.tint(color)
            )
        }
    }
    return bitmap.asAndroidBitmap()
}

fun Bitmap.scaleHeight(newHeight: Int): Bitmap {
    val aspectRatio = width.toFloat() / height.toFloat()
    val newWidth = (newHeight * aspectRatio).toInt()
    return scale(newWidth, newHeight)
}


fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int): Bitmap {
    val drawable = ResourcesCompat.getDrawable(context.resources, resourceId, context.theme)!!
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
