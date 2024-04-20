package com.sloydev.sevibus.ui.icons

import androidx.annotation.DrawableRes

class ZoomableIcon(
    @DrawableRes private val small: Int, @DrawableRes private val medium: Int, @DrawableRes private val large: Int
) {
    @DrawableRes
    fun getRes(zoom: Float) = when {
        zoom < 17.5f -> small
        zoom > 19f -> large
        else -> medium
    }
}
