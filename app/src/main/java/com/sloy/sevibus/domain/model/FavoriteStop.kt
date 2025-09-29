package com.sloy.sevibus.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.vector.ImageVector
import com.sloy.sevibus.ui.icons.DirectionsBusFill
import com.sloy.sevibus.ui.icons.Home
import com.sloy.sevibus.ui.icons.MapFill
import com.sloy.sevibus.ui.icons.SevIcons
import com.sloy.sevibus.ui.icons.Stop

data class FavoriteStop(
    val stop: Stop,
    val customName: String?,
    val customIcon: CustomIcon?,
)

enum class CustomIcon {
    Home, Heart, Star, Stop,
    Office, School, Shopping, Health,
}

fun CustomIcon?.toImageVector(): ImageVector = when (this) {
    CustomIcon.Home -> SevIcons.Home
    CustomIcon.Heart -> Icons.Rounded.Favorite
    CustomIcon.Office -> Icons.Rounded.Work
    CustomIcon.Star -> Icons.Rounded.Star
    CustomIcon.School -> Icons.Rounded.School
    CustomIcon.Shopping -> Icons.Rounded.ShoppingCart
    CustomIcon.Health -> Icons.Rounded.LocalHospital
    CustomIcon.Stop, null -> SevIcons.Stop
    else -> SevIcons.Stop
}
