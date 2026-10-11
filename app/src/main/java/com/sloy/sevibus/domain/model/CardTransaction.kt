package com.sloy.sevibus.domain.model

import java.time.LocalDateTime

enum class CardTransactionKind { TRIP, TRANSFER, TOP_UP, PASS_PURCHASE, PASS_RENEWAL }

data class CardTransaction(
    val serialNumber: CardId,
    val date: LocalDateTime,
    val kind: CardTransactionKind,
    val line: LineSummary? = null,
    val amount: Int? = null,
    val balanceAfter: Int? = null,
    val people: Int = 1,
    val pending: Boolean = false,
)
