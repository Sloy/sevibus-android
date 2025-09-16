package com.sloy.sevibus.feature.foryou.alert

import com.sloy.sevibus.domain.model.CardId

sealed interface AlertState {
    data object Hidden : AlertState
    data class LowBalance(val cardId: CardId) : AlertState
    data class NegativeBalance(val cardId: CardId) : AlertState
}