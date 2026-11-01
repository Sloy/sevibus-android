package com.sloy.sevibus.domain.model

val LOW_BALANCE_THRESHOLD = Money(100)

/**
 * Estimates how many trips a card can still pay, using the fare per person of its most recent paid trip.
 *
 * The balance may go negative, but a card with a negative or zero balance cannot be used. Any positive
 * balance allows one more trip even if it does not cover the full fare, so the result is rounded up:
 * with a 40 cent fare, 60 cents give 2 trips and 15 cents give 1 trip.
 *
 * Returns null when the fare is unknown (no paid trip) or the balance is not positive.
 */
fun estimatedTrips(balance: Money, transactions: List<CardTransaction>): Int? {
    val tripCost = transactions
        .filterIsInstance<CardTransaction.Trip>()
        .filter { it.fare?.isFree == false }
        .maxByOrNull { it.date }
        ?.let { it.fare!!.perPerson(it.people).cents }
        ?: return null
    return if (balance.isPositive) (balance.cents + tripCost - 1) / tripCost else null
}
