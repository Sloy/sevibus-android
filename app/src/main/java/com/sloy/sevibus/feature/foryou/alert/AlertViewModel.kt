package com.sloy.sevibus.feature.foryou.alert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloy.sevibus.domain.repository.CardsRepository
import com.sloy.sevibus.infrastructure.SevLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AlertViewModel(
    private val cardsRepository: CardsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<AlertState>(AlertState.Hidden)
    val state: StateFlow<AlertState> = _state

    init {
        checkCardBalances()
    }

    private fun checkCardBalances() {
        viewModelScope.launch {
            runCatching { cardsRepository.obtainUserCards() }
                .onSuccess { cards ->
                    val alertCard = cards.firstOrNull { card ->
                        card.balance != null && card.balance < 300 // 3€ = 300 cents
                    }

                    _state.value = when {
                        alertCard == null -> AlertState.Hidden
                        alertCard.balance != null && alertCard.balance < 0 -> AlertState.NegativeBalance(alertCard.serialNumber)
                        alertCard.balance != null && alertCard.balance < 300 -> AlertState.LowBalance(alertCard.serialNumber)
                        else -> AlertState.Hidden
                    }
                }
                .onFailure { error ->
                    SevLogger.logW(error, "Error checking card balances")
                    _state.value = AlertState.Hidden
                }
        }
    }
}