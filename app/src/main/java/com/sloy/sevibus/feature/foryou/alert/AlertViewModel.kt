package com.sloy.sevibus.feature.foryou.alert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloy.sevibus.domain.model.CardId
import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.domain.repository.CardsRepository
import com.sloy.sevibus.infrastructure.SevLogger
import com.sloy.sevibus.infrastructure.analytics.Analytics
import com.sloy.sevibus.infrastructure.analytics.SevEvent
import com.sloy.sevibus.infrastructure.analytics.events.Events
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlertViewModel(
    private val cardsRepository: CardsRepository,
    private val analytics: Analytics,
) : ViewModel() {

    val state: StateFlow<AlertState> = combine(
        cardsRepository.observeUserCards(),
        cardsRepository.observeDismissedCardIds().distinctUntilChanged()
    ) { cards, dismissedCardIds ->

        clearDismissedAlertsWithHighBalance(cards, dismissedCardIds)

        val cardsWithAlerts = cards
            .filterLowBalance()
            .filterNot { it.serialNumber in dismissedCardIds }

        cardsWithAlerts
            .firstOrNull()
            ?.let { card ->
                when {
                    card.balance!! < 0 -> AlertState.NegativeBalance(card.serialNumber)
                    else -> AlertState.LowBalance(card.serialNumber)
                }
            } ?: AlertState.Hidden
    }
        .distinctUntilChanged()
        .onEach { state ->
            if (state is AlertState.LowBalance) {
                onTrack(Events.CardAlertDisplayed("low"))
            } else if (state is AlertState.NegativeBalance) {
                onTrack(Events.CardAlertDisplayed("negative"))
            }
        }.catch { error ->
            SevLogger.logW(error, "Error observing card balances")
            emit(AlertState.Hidden)
        }.stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5000),
            initialValue = AlertState.Hidden
        )

    fun onDismissAlert() {
        viewModelScope.launch {
            runCatching {
                val lowBalanceCardIds = cardsRepository.obtainUserCards().filterLowBalance()
                if (lowBalanceCardIds.isNotEmpty()) {
                    cardsRepository.dismissAlertForCards(lowBalanceCardIds.map { it.serialNumber })
                }
            }.onFailure { error ->
                SevLogger.logW(error, "Error dismissing alert")
            }
        }
    }

    fun onTrack(event: SevEvent) {
        analytics.track(event)
    }

    private fun List<CardInfo>.filterLowBalance(): List<CardInfo> = filter { it.balance != null && it.balance < BALANCE_THRESHOLD }
    private fun List<CardInfo>.filterHighBalance(): List<CardInfo> = filter { it.balance != null && it.balance >= BALANCE_THRESHOLD }

    private suspend fun clearDismissedAlertsWithHighBalance(
        cards: List<CardInfo>,
        dismissedCardIds: List<CardId>
    ) {
        cards
            .filterHighBalance()
            .filter { it.serialNumber in dismissedCardIds }
            .forEach {
                SevLogger.logD("Clearing dismissed alerts for: $it")
                cardsRepository.clearDismissedAlert(it.serialNumber)
            }
    }
}

private const val BALANCE_THRESHOLD = 300