package com.sloy.sevibus.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.vector.ImageVector
import com.sloy.sevibus.ui.icons.Home
import com.sloy.sevibus.ui.icons.SevIcons
import com.sloy.sevibus.ui.icons.Stop

data class FavoriteStop(
    val stop: Stop,
    val customName: String?,
    val customIcon: CustomIcon?,
)

enum class CustomIcon {
    Home, Office, Stop
}

fun CustomIcon?.toImageVector(): ImageVector = when (this) {
    CustomIcon.Home -> SevIcons.Home
    CustomIcon.Office -> Icons.Rounded.Work
    CustomIcon.Stop, null -> SevIcons.Stop
}
