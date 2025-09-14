package com.sloy.sevibus.feature.foryou.alert

import com.sloy.sevibus.domain.model.CardId

sealed interface AlertState {
    data object Hidden : AlertState
    data class CardAlert(val cardId: CardId, val message: String) : AlertState
}