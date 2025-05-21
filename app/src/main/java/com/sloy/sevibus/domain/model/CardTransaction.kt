package com.sloy.sevibus.domain.model

import java.time.LocalDateTime

sealed class CardTransaction() {
    data class Validation(
         val serialNumber: CardId,
         val date: LocalDateTime,
        val amount: Int,
        val line: LineSummary,
        val bus: BusId,
        val people: Int,
    ) : CardTransaction()

    data class Transfer(
         val serialNumber: CardId,
         val date: LocalDateTime,
        val line: LineSummary,
        val bus: BusId,
        val people: Int,
    ) : CardTransaction()

    data class TopUp(
         val serialNumber: CardId,
         val date: LocalDateTime,
        val amount: Int,
    ) : CardTransaction()

}
