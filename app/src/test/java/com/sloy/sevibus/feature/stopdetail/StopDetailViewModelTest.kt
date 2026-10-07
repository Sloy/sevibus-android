package com.sloy.sevibus.feature.stopdetail

import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.Bus
import com.sloy.sevibus.domain.model.BusArrival
import com.sloy.sevibus.domain.model.RouteId
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.domain.repository.BusRepository
import com.sloy.sevibus.domain.repository.FavoriteRepository
import com.sloy.sevibus.domain.repository.StopRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
import org.mockito.kotlin.whenever
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo

@OptIn(ExperimentalCoroutinesApi::class)
class StopDetailViewModelTest {

    private val stop = Stubs.stops[0]
    private val stopRepository = mock<StopRepository>()
    private val arrivalsResponse = CompletableDeferred<List<BusArrival>>()
    private val busRepository = object : BusRepository {
        override suspend fun obtainBusArrivals(stop: StopId): List<BusArrival> = arrivalsResponse.await()
        override suspend fun obtainBuses(route: RouteId): List<Bus> = emptyList()
    }
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

    private fun TestScope.collectState(): () -> StopDetailScreenState {
        val viewModel = StopDetailViewModel(stop.code, stopRepository, busRepository, favoriteRepository, mock(), mock())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
        return { viewModel.state.value }
    }
}
