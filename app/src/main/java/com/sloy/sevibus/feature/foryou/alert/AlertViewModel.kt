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
        fetchAlert()
    }

    private fun fetchAlert() {
        viewModelScope.launch {
            runCatching { cardsRepository.getCardAlert() }
                .onSuccess { alert ->
                    _state.value = if (alert != null) {
                        AlertState.CardAlert(cardId = alert.cardId, message = alert.message)
                    } else {
                        AlertState.Hidden
                    }
                }
                .onFailure { error ->
                    SevLogger.logW(error, "Error obtaining card alert")
                    _state.value = AlertState.Hidden
                }
        }
    }
}