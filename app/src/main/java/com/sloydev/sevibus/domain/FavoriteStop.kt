package com.sloydev.sevibus.domain

data class FavoriteStop(
    val customName: String,
    val code: Int,
    val description: String,
    val position: Stop.Position,
)
