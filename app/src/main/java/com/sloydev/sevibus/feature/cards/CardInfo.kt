package com.sloydev.sevibus.feature.cards

import java.time.LocalDate

data class CardInfo(
    val code: Int,
    val title: String,
    val customName: String?,
    val serialNumber: Long,
    val expiration: LocalDate? = null,
    val validityEnd: LocalDate? = null,
    val extensionEnd: LocalDate? = null,
    val balanceMillis: Int? = null,
    val balanceTrips: Int? = null
)

