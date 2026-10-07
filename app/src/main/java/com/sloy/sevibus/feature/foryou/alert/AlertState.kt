package com.sloy.sevibus.feature.foryou.alert

import com.sloy.sevibus.domain.model.CardInfo

sealed interface AlertState {
    data object Hidden : AlertState
    data class LowBalance(val card: CardInfo) : AlertState
    data class NegativeBalance(val card: CardInfo) : AlertState
}