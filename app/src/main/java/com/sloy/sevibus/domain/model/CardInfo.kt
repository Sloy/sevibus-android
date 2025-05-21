package com.sloy.sevibus.domain.model

import com.sloy.sevibus.feature.cards.CardSerialNumberUtils

typealias CardId = Long

data class CardInfo(
    val serialNumber: CardId,
    val code: Int,
    val type: String,
    val balance: Int? = null,
    val customName: String? = null,
) {

    val fullSerialNumber: String by lazy {
        CardSerialNumberUtils.calculateVisibleSerialNumber(serialNumber)
    }

    val formattedSerialNumber: String
        get() = fullSerialNumber.chunked(4).joinToString(" ")

}



