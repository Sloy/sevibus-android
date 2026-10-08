package com.sloy.sevibus.feature.cards

import com.sloy.sevibus.domain.model.CardAddMethod
import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.domain.repository.CardsRepository
import com.sloy.sevibus.infrastructure.analytics.Analytics
import com.sloy.sevibus.infrastructure.analytics.events.Events
import com.sloy.sevibus.infrastructure.analytics.events.Events.CardCheckCompleted.Result
import com.sloy.sevibus.infrastructure.analytics.events.Events.CardScanned.ScanMethod
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
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class CardViewModelTest {

    private val savedCard = CardInfo(serialNumber = SAVED_SERIAL, code = 31, type = "Bonobús", balance = 1250)
    private val newCard = CardInfo(serialNumber = NEW_SERIAL, code = 31, type = "Bonobús", balance = 500)
    private val userCards = MutableStateFlow(listOf(savedCard))
    private val cardsRepository = mock<CardsRepository> {
        on { observeUserCards() } doReturn userCards
        onBlocking { obtainTransactions(any()) } doReturn emptyList()
    }
    private val analytics = mock<Analytics>()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `card check completes as added when the card is new`() = runTest {
        stubCheckCard(returns = newCard)

        collectingViewModel().onNewCardNumber(NEW_SERIAL.toString(), CardAddMethod.MANUAL)

        verify(analytics).track(Events.CardCheckCompleted(ScanMethod.MANUAL, Result.ADDED))
    }

    @Test
    fun `card check completes as already saved when the user has the card`() = runTest {
        stubCheckCard(returns = savedCard)

        collectingViewModel().onNewCardNumber(SAVED_SERIAL.toString(), CardAddMethod.NFC)

        verify(analytics).track(Events.CardCheckCompleted(ScanMethod.NFC, Result.ALREADY_SAVED))
    }

    @Test
    fun `card check completes as not found when the card doesn't exist`() = runTest {
        stubCheckCard(returns = null)

        collectingViewModel().onNewCardNumber(NEW_SERIAL.toString(), CardAddMethod.MANUAL)

        verify(analytics).track(Events.CardCheckCompleted(ScanMethod.MANUAL, Result.NOT_FOUND))
    }

    @Test
    fun `card check completes as error when the lookup fails`() = runTest {
        cardsRepository.stub {
            onBlocking { checkCard(any(), any()) } doThrow IllegalStateException("Server error")
        }

        collectingViewModel().onNewCardNumber(NEW_SERIAL.toString(), CardAddMethod.NFC)

        verify(analytics).track(Events.CardCheckCompleted(ScanMethod.NFC, Result.ERROR))
    }

    private fun stubCheckCard(returns: CardInfo?) {
        cardsRepository.stub {
            onBlocking { checkCard(any(), any()) } doReturn returns
        }
    }

    private fun TestScope.collectingViewModel(): CardViewModel {
        val viewModel = CardViewModel(cardsRepository, analytics)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
        return viewModel
    }
}

private const val SAVED_SERIAL = 123456789012L
private const val NEW_SERIAL = 987654321098L
