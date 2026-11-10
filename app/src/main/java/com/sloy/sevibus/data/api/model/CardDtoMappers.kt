package com.sloy.sevibus.data.api.model

import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.domain.model.Money

fun CardInfoDto.fromDto(): CardInfo = when (balance) {
    null -> CardInfo.PassCard(serialNumber, code, type, customName)
    else -> CardInfo.BalanceCard(serialNumber, code, type, Money(balance), customName)
}
