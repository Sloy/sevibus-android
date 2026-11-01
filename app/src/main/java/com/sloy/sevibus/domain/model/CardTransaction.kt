package com.sloy.sevibus.domain.model

import java.time.LocalDateTime

sealed interface CardTransaction {
    val serialNumber: CardId
    val date: LocalDateTime

    data class Trip(
        override val serialNumber: CardId,
        override val date: LocalDateTime,
        val line: LineSummary,
        val people: Int,
        val fare: Fare?,
        val transfers: List<Transfer> = emptyList(),
    ) : CardTransaction

    data class Transfer(
        override val serialNumber: CardId,
        override val date: LocalDateTime,
        val line: LineSummary,
    ) : CardTransaction

    data class TopUp(
        override val serialNumber: CardId,
        override val date: LocalDateTime,
        val amount: Money,
        val balanceAfter: Money,
    ) : CardTransaction

    data class PendingPurchase(
        override val serialNumber: CardId,
        override val date: LocalDateTime,
        val amount: Money,
    ) : CardTransaction

    data class PassPurchase(
        override val serialNumber: CardId,
        override val date: LocalDateTime,
        val amount: Money,
    ) : CardTransaction

    data class PassRenewal(
        override val serialNumber: CardId,
        override val date: LocalDateTime,
        val amount: Money?,
    ) : CardTransaction
}

data class Fare(val total: Money, val balanceAfter: Money) {
    val isFree: Boolean get() = !total.isPositive
    fun perPerson(people: Int): Money = total / people
}
