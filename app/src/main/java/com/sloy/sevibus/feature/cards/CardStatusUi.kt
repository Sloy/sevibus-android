package com.sloy.sevibus.feature.cards

import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.domain.model.CardTransaction
import com.sloy.sevibus.domain.model.Money
import com.sloy.sevibus.domain.model.PassValidity
import com.sloy.sevibus.domain.model.estimatedTrips
import java.time.LocalDate
import java.time.temporal.ChronoUnit

sealed interface CardStatusUi {
    data class Balance(val balance: Money, val isEmpty: Boolean, val isLow: Boolean, val tripsLeft: Int?) : CardStatusUi
    data class Pass(val validTo: LocalDate, val hasNextPeriod: Boolean, val progress: PassProgress?, val expiredAgo: Elapsed?) : CardStatusUi
    data class ExpiredCard(val expiredOn: LocalDate?) : CardStatusUi
    data object None : CardStatusUi
}

data class PassProgress(val validFrom: LocalDate, val fraction: Float, val daysLeft: Int)

data class CardDetailsUi(val serial: String, val period: PeriodRow?, val physicalExpiry: LocalDate?)

data class PeriodRow(val from: LocalDate, val to: LocalDate, val isCurrentPeriod: Boolean)

fun cardStatusUi(card: CardInfo, transactions: List<CardTransaction>, today: LocalDate): CardStatusUi = when (card) {
    is CardInfo.BalanceCard -> CardStatusUi.Balance(
        balance = card.balance,
        isEmpty = card.isEmpty,
        isLow = !card.isEmpty && card.isLowBalance,
        tripsLeft = estimatedTrips(card.balance, transactions),
    )
    is CardInfo.PassCard -> card.validity?.let { passStatusUi(it, today) } ?: CardStatusUi.None
    is CardInfo.ExpiredCard -> CardStatusUi.ExpiredCard(card.expiresOn)
}

private fun passStatusUi(validity: PassValidity, today: LocalDate): CardStatusUi.Pass {
    val (validFrom, validTo) = validity.period
    if (today.isAfter(validTo)) {
        return CardStatusUi.Pass(validTo, hasNextPeriod = false, progress = null, expiredAgo = CardDateFormatter.elapsed(validTo, today))
    }
    val hasNextPeriod = validity.nextPeriod != null
    if (today.isBefore(validFrom)) {
        return CardStatusUi.Pass(validTo, hasNextPeriod, progress = null, expiredAgo = null)
    }
    val totalDays = ChronoUnit.DAYS.between(validFrom, validTo) + 1
    val daysLeft = ChronoUnit.DAYS.between(today, validTo)
    val fraction = ((totalDays - daysLeft).toFloat() / totalDays).coerceIn(0f, 1f)
    return CardStatusUi.Pass(validTo, hasNextPeriod, PassProgress(validFrom, fraction, daysLeft.toInt()), expiredAgo = null)
}

fun cardDetailsUi(card: CardInfo, today: LocalDate): CardDetailsUi {
    val validity = (card as? CardInfo.PassCard)?.validity
    val period = validity?.let { PeriodRow(it.period.from, it.period.to, isCurrentPeriod = it.nextPeriod != null && !today.isAfter(it.period.to)) }
    val physicalExpiry = card.expiresOn.takeIf { card !is CardInfo.ExpiredCard }
    return CardDetailsUi(card.formattedSerialNumber, period, physicalExpiry)
}
