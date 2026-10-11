package com.sloy.sevibus.domain.model

import java.time.LocalDate

data class NextPeriod(val from: LocalDate?, val to: LocalDate?, val pendingFirstUse: Boolean)

sealed interface CardStatus {
    data class Balance(val balance: Int, val alertCode: Int? = null) : CardStatus
    data class Pass(val validFrom: LocalDate?, val validTo: LocalDate?, val nextPeriod: NextPeriod?) : CardStatus
    data object ExpiredCard : CardStatus
}
