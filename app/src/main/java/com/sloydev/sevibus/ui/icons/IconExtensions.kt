package com.sloydev.sevibus.ui.icons

import android.graphics.Bitmap
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
import com.sloydev.sevibus.R


@Composable
fun ImageVector.asBitmap(tint: Color): Bitmap {
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
                colorFilter = ColorFilter.tint(tint)
            )
        }
    }
    return bitmap.asAndroidBitmap()
}

@Composable
fun layeredBitmap(vararg icons: Pair<ImageVector, Color>): Bitmap {
    check(icons.size > 0)
    val painter = rememberVectorPainter(icons.first().first)
    val size = painter.intrinsicSize
    val bitmap = ImageBitmap(size.width.toInt(), size.height.toInt())
    val canvas = androidx.compose.ui.graphics.Canvas(bitmap)

    CanvasDrawScope().draw(
        density = Density(1f),
        layoutDirection = LayoutDirection.Ltr,
        canvas = canvas,
        size = size,
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

    return bitmap.asAndroidBitmap()
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
