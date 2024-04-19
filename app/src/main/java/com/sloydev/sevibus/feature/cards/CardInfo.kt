package com.sloydev.sevibus.feature.cards

data class CardInfo(
    val code: Int,
    val title: String,
    val customName: String?,
    val balanceMillis: Int? = null,
    val balanceTrips: Int? = null
)

