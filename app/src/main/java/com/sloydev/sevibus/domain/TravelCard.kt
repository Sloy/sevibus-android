package com.sloydev.sevibus.domain

import java.time.LocalDate
import java.time.LocalDateTime

data class TravelCard(
    val customName: String? = null,
    val serialNumber: Long,
    val title: String,
    val code: Int,
    val balanceMillis: Int? = null,
    val balanceTrips: Int? = null,
    val expiration: LocalDate? = null,
    val validityEnd: LocalDate? = null,
    val extensionEnd: LocalDate? = null,
) {

    sealed class Transaction(open val amountMillis: Int, open val date: LocalDateTime) {
        data class Trip(
            override val amountMillis: Int,
            override val date: LocalDateTime,
            val line: Line,
            val people: Int = 1
        ) : Transaction(amountMillis, date)

        data class TopUp(
            override val amountMillis: Int,
            override val date: LocalDateTime
        ) : Transaction(amountMillis, date)

    }

}



