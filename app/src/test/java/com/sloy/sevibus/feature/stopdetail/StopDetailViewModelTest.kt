package com.sloy.sevibus.feature.stopdetail

import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.Bus
import com.sloy.sevibus.domain.model.BusArrival
import com.sloy.sevibus.domain.model.RouteId
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.domain.repository.BusRepository
import com.sloy.sevibus.domain.repository.FavoriteRepository
import com.sloy.sevibus.domain.repository.StopRepository
import com.sloy.sevibus.infrastructure.analytics.Analytics
import com.sloy.sevibus.infrastructure.analytics.events.Events
import com.sloy.sevibus.infrastructure.analytics.events.Screens
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import java.net.SocketTimeoutException
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class StopDetailViewModelTest {

    private val stop = Stubs.stops[0]
    private val stopRepository = mock<StopRepository>()
    private val arrivalsResponse = CompletableDeferred<List<BusArrival>>()
    private var obtainArrivals: suspend () -> List<BusArrival> = { arrivalsResponse.await() }
    private val busRepository = object : BusRepository {
        override suspend fun obtainBusArrivals(stop: StopId): List<BusArrival> = obtainArrivals()
        override suspend fun obtainBuses(route: RouteId): List<Bus> = emptyList()
    }
    private val analytics = mock<Analytics>()
    private val favoriteRepository = mock<FavoriteRepository> {
        on { observeFavorites() } doReturn flowOf(emptyList())
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `shows loading arrivals until the first response arrives`() = runTest {
        whenever(stopRepository.obtainStop(stop.code)).thenReturn(stop)

        val state = collectState()

        expectThat(state()).isA<StopDetailScreenState.Loaded>().get { arrivalsState }
            .isEqualTo(ArrivalsState.Loading(stop.lines))
    }

    @Test
    fun `shows loaded arrivals when the response has arrivals`() = runTest {
        whenever(stopRepository.obtainStop(stop.code)).thenReturn(stop)
        arrivalsResponse.complete(Stubs.arrivals)

        val state = collectState()

        expectThat(state()).isA<StopDetailScreenState.Loaded>().get { arrivalsState }
            .isEqualTo(ArrivalsState.Loaded(Stubs.arrivals))
    }

    @Test
    fun `shows failed arrivals when the response is empty`() = runTest {
        whenever(stopRepository.obtainStop(stop.code)).thenReturn(stop)
        arrivalsResponse.complete(emptyList())

        val state = collectState()

        expectThat(state()).isA<StopDetailScreenState.Loaded>().get { arrivalsState }
            .isA<ArrivalsState.Failed>()
    }

    @Test
    fun `tracks arrivals displayed once per screen view, not on every poll`() = runTest {
        whenever(stopRepository.obtainStop(stop.code)).thenReturn(stop)
        obtainArrivals = { Stubs.arrivals }
        val viewModel = collectingViewModel()

        viewModel.onScreenStarted()
        advanceTimeBy(65.seconds)

        verify(analytics, times(1)).track(any<Events.ArrivalsDisplayed>())
        verify(analytics).track(
            Events.ArrivalsDisplayed(Events.ArrivalsScreen.STOP_DETAIL, stop.code, null, Stubs.arrivals.size, latencyMs = 0)
        )
    }

    @Test
    fun `tracks arrivals displayed with the latency since the screen was opened`() = runTest {
        whenever(stopRepository.obtainStop(stop.code)).thenReturn(stop)
        obtainArrivals = {
            delay(1500)
            Stubs.arrivals
        }
        val viewModel = createViewModel()

        viewModel.onScreenStarted()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
        advanceTimeBy(2.seconds)

        verify(analytics).track(
            Events.ArrivalsDisplayed(Events.ArrivalsScreen.STOP_DETAIL, stop.code, null, Stubs.arrivals.size, latencyMs = 1500)
        )
    }

    @Test
    fun `tracks arrivals displayed again on a new screen view`() = runTest {
        whenever(stopRepository.obtainStop(stop.code)).thenReturn(stop)
        obtainArrivals = { Stubs.arrivals }
        val viewModel = collectingViewModel()

        viewModel.onScreenStarted()
        advanceTimeBy(30.seconds)
        viewModel.onScreenStopped()
        viewModel.onScreenStarted()
        advanceTimeBy(30.seconds)

        verify(analytics, times(2)).track(any<Events.ArrivalsDisplayed>())
    }

    @Test
    fun `tracks stop details closed with the duration and the successful polls`() = runTest {
        whenever(stopRepository.obtainStop(stop.code)).thenReturn(stop)
        obtainArrivals = { Stubs.arrivals }
        val viewModel = collectingViewModel()

        viewModel.onScreenStarted()
        advanceTimeBy(65.seconds)
        viewModel.onScreenStopped()

        verify(analytics).track(Screens.StopDetailsClosed(stop.code, durationSeconds = 65, refreshes = 3))
    }

    @Test
    fun `tracks arrivals failed on every failed poll`() = runTest {
        whenever(stopRepository.obtainStop(stop.code)).thenReturn(stop)
        obtainArrivals = { throw SocketTimeoutException() }
        val viewModel = collectingViewModel()

        viewModel.onScreenStarted()
        advanceTimeBy(45.seconds)

        verify(analytics, times(3)).track(
            Events.ArrivalsFailed(Events.ArrivalsScreen.STOP_DETAIL, stop.code, Events.ArrivalsFailed.ErrorType.TIMEOUT)
        )
        verify(analytics, never()).track(any<Events.ArrivalsDisplayed>())
    }

    private fun TestScope.createViewModel() = StopDetailViewModel(
        stop.code, stopRepository, busRepository, favoriteRepository, mock(), analytics, clock = { testScheduler.currentTime }
    )

    private fun TestScope.collectingViewModel(): StopDetailViewModel {
        val viewModel = createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
        return viewModel
    }

    private fun TestScope.collectState(): () -> StopDetailScreenState {
        val viewModel = collectingViewModel()
        return { viewModel.state.value }
    }
}
