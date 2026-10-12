package com.sloy.sevibus.domain.model

import com.sloy.sevibus.feature.cards.CardSerialNumberUtils
import java.time.LocalDate

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
    val expiresOn: LocalDate?

    val fullSerialNumber: String
        get() = CardSerialNumberUtils.calculateVisibleSerialNumber(serialNumber)

    val formattedSerialNumber: String
        get() = fullSerialNumber.chunked(4).joinToString(" ")

    fun withCustomName(customName: String?): CardInfo = when (this) {
        is BalanceCard -> copy(customName = customName)
        is PassCard -> copy(customName = customName)
        is ExpiredCard -> copy(customName = customName)
    }

    data class BalanceCard(
        override val serialNumber: CardId,
        override val code: Int,
        override val type: String,
        val balance: Money,
        override val customName: String? = null,
        override val expiresOn: LocalDate? = null,
    ) : CardInfo {
        val isEmpty: Boolean get() = !balance.isPositive
        val isLowBalance: Boolean get() = balance < LOW_BALANCE_THRESHOLD
    }

    data class PassCard(
        override val serialNumber: CardId,
        override val code: Int,
        override val type: String,
        val validity: PassValidity?,
        override val customName: String? = null,
        override val expiresOn: LocalDate? = null,
    ) : CardInfo

    data class ExpiredCard(
        override val serialNumber: CardId,
        override val code: Int,
        override val type: String,
        override val customName: String? = null,
        override val expiresOn: LocalDate? = null,
    ) : CardInfo
}

data class PassValidity(val period: PassPeriod, val nextPeriod: NextPeriod?)

data class PassPeriod(val from: LocalDate, val to: LocalDate)

sealed interface NextPeriod {
    data class Scheduled(val period: PassPeriod) : NextPeriod
    data object OnFirstUse : NextPeriod
}
