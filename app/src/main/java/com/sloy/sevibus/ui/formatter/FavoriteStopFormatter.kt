package com.sloy.sevibus.ui.formatter

import com.sloy.sevibus.domain.model.FavoriteStop
import com.sloy.sevibus.domain.model.Stop
import com.sloy.sevibus.domain.model.description1
import com.sloy.sevibus.domain.model.description2
import com.sloy.sevibus.domain.model.descriptionWithSeparator

fun FavoriteStop.formatTitle(): String = stop.formatTitle(this)
fun Stop.formatTitle(favorite: FavoriteStop?): String {
    return favorite?.customName ?: this.description1
}

fun FavoriteStop.formatSubtitle(): String? = stop.formatSubtitle(this)
fun Stop.formatSubtitle(favorite: FavoriteStop?): String? {
    val subtitle = if (favorite?.customName != null) {
        this.descriptionWithSeparator()
    } else if (this.description2 != null) {
        this.description2
    } else {
        null
    }
    return subtitle
}
