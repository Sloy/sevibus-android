package com.sloy.sevibus.feature.cards

import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.domain.model.CardStatus
import com.sloy.sevibus.domain.model.CardTransaction
import com.sloy.sevibus.domain.model.LOW_BALANCE_THRESHOLD
import com.sloy.sevibus.domain.model.estimatedTrips
import java.time.LocalDate
import java.time.temporal.ChronoUnit

sealed interface CardStatusUi {
    data class Balance(val balance: Int, val isEmpty: Boolean, val isLow: Boolean, val tripsLeft: Int?) : CardStatusUi
    data class Pass(val validTo: LocalDate, val hasNextPeriod: Boolean, val progress: PassProgress?, val expiredAgo: Elapsed?) : CardStatusUi
    data class ExpiredCard(val expiredOn: LocalDate?) : CardStatusUi
    data object None : CardStatusUi
}

data class PassProgress(val validFrom: LocalDate, val fraction: Float, val daysLeft: Int)

data class CardDetailsUi(val serial: String, val period: PeriodRow?, val physicalExpiry: LocalDate?)

data class PeriodRow(val from: LocalDate, val to: LocalDate, val isCurrentPeriod: Boolean)

fun cardStatusUi(card: CardInfo, transactions: List<CardTransaction>, today: LocalDate): CardStatusUi =
    when (val status = card.status) {
        is CardStatus.Balance -> {
            val isEmpty = status.balance <= 0
            CardStatusUi.Balance(
                balance = status.balance,
                isEmpty = isEmpty,
                isLow = !isEmpty && status.balance < LOW_BALANCE_THRESHOLD,
                tripsLeft = estimatedTrips(status.balance, transactions),
            )
        }
        is CardStatus.Pass -> passStatusUi(status, today)
        CardStatus.ExpiredCard -> CardStatusUi.ExpiredCard(card.cardExpiry)
    }

private fun passStatusUi(status: CardStatus.Pass, today: LocalDate): CardStatusUi {
    val validTo = status.validTo ?: return CardStatusUi.None
    val validFrom = status.validFrom
    if (today.isAfter(validTo)) {
        return CardStatusUi.Pass(validTo, hasNextPeriod = false, progress = null, expiredAgo = CardDateFormatter.elapsed(validTo, today))
    }
    val hasNextPeriod = status.nextPeriod != null
    if (validFrom == null || today.isBefore(validFrom)) {
        return CardStatusUi.Pass(validTo, hasNextPeriod, progress = null, expiredAgo = null)
    }
    val totalDays = ChronoUnit.DAYS.between(validFrom, validTo) + 1
    val daysLeft = ChronoUnit.DAYS.between(today, validTo)
    val fraction = ((totalDays - daysLeft).toFloat() / totalDays).coerceIn(0f, 1f)
    return CardStatusUi.Pass(validTo, hasNextPeriod, PassProgress(validFrom, fraction, daysLeft.toInt()), expiredAgo = null)
}

fun cardDetailsUi(card: CardInfo, today: LocalDate): CardDetailsUi {
    val pass = card.status as? CardStatus.Pass
    val period = pass?.let { status ->
        val from = status.validFrom ?: return@let null
        val to = status.validTo ?: return@let null
        PeriodRow(from, to, isCurrentPeriod = status.nextPeriod != null && !today.isAfter(to))
    }
    val physicalExpiry = card.cardExpiry.takeIf { card.status !is CardStatus.ExpiredCard }
    return CardDetailsUi(card.formattedSerialNumber, period, physicalExpiry)
}
