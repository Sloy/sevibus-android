package com.sloy.sevibus.domain.model

import com.sloy.sevibus.feature.cards.CardSerialNumberUtils
import java.time.LocalDate

typealias CardId = Long

enum class CardAddMethod {
    NFC,
    MANUAL,
}

data class CardInfo(
    val serialNumber: CardId,
    val code: Int,
    val type: String,
    val status: CardStatus,
    val customName: String? = null,
    val cardExpiry: LocalDate? = null,
    val resultCode: Int? = null,
) {

    val balance: Int?
        get() = (status as? CardStatus.Balance)?.balance

    val fullSerialNumber: String by lazy {
        CardSerialNumberUtils.calculateVisibleSerialNumber(serialNumber)
    }

    val formattedSerialNumber: String
        get() = fullSerialNumber.chunked(4).joinToString(" ")

}
