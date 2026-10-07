package com.sloy.sevibus.domain.model

import kotlin.math.absoluteValue

const val LOW_BALANCE_THRESHOLD = 100

val CardInfo.hasBalance: Boolean
    get() = balance != null

val CardInfo.isLowBalance: Boolean
    get() = balance != null && balance < LOW_BALANCE_THRESHOLD

/**
 * Estimates how many trips a card can still pay, using the cost of its most recent validation.
 *
 * The balance may go negative, but a card with a negative or zero balance cannot be used. Any positive
 * balance allows one more trip even if it does not cover the full fare, so the result is rounded up:
 * with a 40 cent fare, 60 cents give 2 trips and 15 cents give 1 trip.
 *
 * Returns null when the fare is unknown (no validation with an amount) or the balance is not positive.
 */
fun estimatedTrips(balance: Int, transactions: List<CardTransaction>): Int? {
    val tripCost = transactions
        .filterIsInstance<CardTransaction.Validation>()
        .filter { it.amount != 0 }
        .maxByOrNull { it.date }
        ?.amount
        ?.absoluteValue
        ?: return null
    return if (balance > 0) (balance + tripCost - 1) / tripCost else null
}
