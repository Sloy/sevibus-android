package com.sloy.sevibus.feature.foryou.alert

import com.sloy.sevibus.domain.model.CardId
import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.domain.repository.CardsRepository
import com.sloy.sevibus.infrastructure.analytics.Analytics
import com.sloy.sevibus.infrastructure.analytics.events.Events
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@OptIn(ExperimentalCoroutinesApi::class)
class AlertViewModelTest {

    private val cards = MutableStateFlow<List<CardInfo>>(emptyList())
    private val dismissed = MutableStateFlow<List<CardId>>(emptyList())
    private val cardsRepository = mock<CardsRepository> {
        on { observeUserCards() } doReturn cards
        on { observeDismissedCardIds() } doReturn dismissed
    }

    private val analytics = mock<Analytics>()

    private fun card(serial: CardId, balance: Int?) = CardInfo(serialNumber = serial, code = 31, type = "Bonobús", balance = balance)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `hidden when no card has low balance`() = runTest {
        cards.value = listOf(card(1, 1250), card(2, null))

        val state = collectState()

        expectThat(state()).isEqualTo(AlertState.Hidden)
    }

    @Test
    fun `shows the first low balance card in user order`() = runTest {
        val first = card(1, 80)
        cards.value = listOf(card(3, 1250), first, card(2, 20))

        val state = collectState()

        expectThat(state()).isEqualTo(AlertState.LowBalance(first))
    }

    @Test
    fun `shows negative balance for a card below zero`() = runTest {
        val negative = card(1, -45)
        cards.value = listOf(negative)

        val state = collectState()

        expectThat(state()).isEqualTo(AlertState.NegativeBalance(negative))
    }

    @Test
    fun `skips dismissed cards`() = runTest {
        val second = card(2, 60)
        cards.value = listOf(card(1, 80), second)
        dismissed.value = listOf(1L)

        val state = collectState()

        expectThat(state()).isEqualTo(AlertState.LowBalance(second))
    }

    @Test
    fun `updates the alert card when its balance changes`() = runTest {
        cards.value = listOf(card(1, 80))
        val state = collectState()

        cards.value = listOf(card(1, 90))

        expectThat(state()).isEqualTo(AlertState.LowBalance(card(1, 90)))
    }

    @Test
    fun `tracks the alert once while the same card stays low`() = runTest {
        cards.value = listOf(card(1, 80))
        collectState()

        cards.value = listOf(card(1, 90))

        verify(analytics, times(1)).track(Events.CardAlertDisplayed("low"))
    }

    private fun TestScope.collectState(): () -> AlertState {
        val viewModel = AlertViewModel(cardsRepository, analytics)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
        return { viewModel.state.value }
    }
}
