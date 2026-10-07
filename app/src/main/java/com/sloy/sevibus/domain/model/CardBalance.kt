package com.sloy.sevibus.domain.model

import kotlin.math.absoluteValue

const val LOW_BALANCE_THRESHOLD = 300

val CardInfo.hasBalance: Boolean
    get() = balance != null

val CardInfo.isLowBalance: Boolean
    get() = balance != null && balance < LOW_BALANCE_THRESHOLD

fun estimatedTrips(balance: Int, transactions: List<CardTransaction>): Int? {
    val tripCost = transactions
        .filterIsInstance<CardTransaction.Validation>()
        .filter { it.amount != 0 }
        .maxByOrNull { it.date }
        ?.amount
        ?.absoluteValue
        ?: return null
    return (balance / tripCost).takeIf { it > 0 }
}
