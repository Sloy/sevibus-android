package com.sloydev.sevibus.ui.icons

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import com.sloydev.sevibus.R
import com.sloydev.sevibus.domain.model.LineColor


object SevIcons {
    object MapIcon {
        val stop = ZoomableIcon(R.drawable.map_icon_stop_small, R.drawable.map_icon_stop_medium, R.drawable.map_icon_stop_large)
    }

    object CircularStopMarker {

        fun bitmap(context: Context, color: LineColor): Bitmap {
            val vectorDrawable = ResourcesCompat.getDrawable(context.resources, res(color), null)!!
            val bitmap = Bitmap.createBitmap(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
            //DrawableCompat.setTint(vectorDrawable, color.toArgb())
            vectorDrawable.draw(canvas)
            return bitmap
        }

        @DrawableRes
        private fun res(color: LineColor) = when (color) {
            LineColor.Red -> R.drawable.stop_marker_red
            LineColor.Orange -> R.drawable.stop_marker_orange
            LineColor.Blue -> R.drawable.stop_marker_blue
            LineColor.Cyan -> R.drawable.stop_marker_cyan
            LineColor.Green -> R.drawable.stop_marker_green
            LineColor.Wine -> R.drawable.stop_marker_wine
            LineColor.Black -> R.drawable.stop_marker_black
            LineColor.Unknown -> R.drawable.stop_marker_black
        }
    }
}