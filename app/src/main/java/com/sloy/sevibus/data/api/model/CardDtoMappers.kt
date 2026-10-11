package com.sloy.sevibus.data.api.model

import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.domain.model.CardStatus
import com.sloy.sevibus.domain.model.NextPeriod
import java.time.LocalDate

fun String?.toLocalDateOrNull(): LocalDate? = this?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

fun NextPeriodDto.toDomain(): NextPeriod = NextPeriod(from.toLocalDateOrNull(), to.toLocalDateOrNull(), pendingFirstUse)

fun cardStatusOf(kind: String?, balance: Int?, alertCode: Int?, validFrom: String?, validTo: String?, nextPeriod: NextPeriod?): CardStatus =
    when {
        kind == "expired_card" -> CardStatus.ExpiredCard
        kind != "pass" && balance != null -> CardStatus.Balance(balance, alertCode)
        else -> CardStatus.Pass(validFrom.toLocalDateOrNull(), validTo.toLocalDateOrNull(), nextPeriod)
    }

fun CardInfoDto.toDomain(): CardInfo = CardInfo(
    serialNumber = serialNumber,
    code = code,
    type = type,
    status = cardStatusOf(kind, balance, alertCode, validFrom, validTo, nextPeriod?.toDomain()),
    customName = customName,
    cardExpiry = cardExpiry.toLocalDateOrNull(),
    resultCode = resultCode,
)
