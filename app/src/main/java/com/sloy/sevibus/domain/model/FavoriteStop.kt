package com.sloy.sevibus.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

data class FavoriteStop(
    val customName: String,
    val icon: ImageVector,
    val stop: Stop,
)
