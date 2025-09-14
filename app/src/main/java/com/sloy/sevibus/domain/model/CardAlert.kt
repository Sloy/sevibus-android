package com.sloy.sevibus.domain.model

data class CardAlert(
    val cardId: CardId,
    val type: String,
    val message: String,
)