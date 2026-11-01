package com.sloy.sevibus.domain.model

import com.sloy.sevibus.feature.cards.CardSerialNumberUtils

typealias CardId = Long

enum class CardAddMethod {
    NFC,
    MANUAL,
}

sealed interface CardInfo {
    val serialNumber: CardId
    val code: Int
    val type: String
    val customName: String?

    val fullSerialNumber: String
        get() = CardSerialNumberUtils.calculateVisibleSerialNumber(serialNumber)

    val formattedSerialNumber: String
        get() = fullSerialNumber.chunked(4).joinToString(" ")

    data class BalanceCard(
        override val serialNumber: CardId,
        override val code: Int,
        override val type: String,
        val balance: Money,
        override val customName: String? = null,
    ) : CardInfo {
        val isEmpty: Boolean get() = !balance.isPositive
        val isLowBalance: Boolean get() = balance < LOW_BALANCE_THRESHOLD
    }

    data class PassCard(
        override val serialNumber: CardId,
        override val code: Int,
        override val type: String,
        override val customName: String? = null,
    ) : CardInfo
}
