package com.sloydev.sevibus.ui.icons

import androidx.annotation.DrawableRes

class ZoomableIcon(
    @DrawableRes private val small: Int, @DrawableRes private val medium: Int, @DrawableRes private val large: Int
) {
    @DrawableRes
    fun getByZoom(zoom: Int) = when {
        zoom < 17 -> small
        zoom >= 19 -> large
        else -> medium
    }
}
