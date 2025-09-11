package com.sloy.sevibus.data.api

import com.sloy.sevibus.data.api.model.BusArrivalDto
import com.sloy.sevibus.data.api.model.BusDto
import com.sloy.sevibus.data.api.model.CardInfoDto
import com.sloy.sevibus.data.api.model.CardTransactionDto
import com.sloy.sevibus.data.api.model.LineDto
import com.sloy.sevibus.data.api.model.PathDto
import com.sloy.sevibus.data.api.model.RouteDto
import com.sloy.sevibus.data.api.model.StopDto
import com.sloy.sevibus.domain.model.CardId
import com.sloy.sevibus.domain.model.RouteId
import com.sloy.sevibus.domain.model.StopId
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


open class FakeSevibusApi : SevibusApi {

    var linesResponse: List<LineDto> = emptyList()
    var routesResponse: List<RouteDto> = emptyList()
    var stopsResponse: List<StopDto> = emptyList()
    var arrivalsResponse: List<BusArrivalDto> = emptyList()
    var pathResponse: PathDto? = null
    var pathsResponse: List<PathDto> = emptyList()
    var busesResponse: List<BusDto> = emptyList()
    var cardInfoResponse: CardInfoDto? = null
    var cardTransactionsResponse: List<CardTransactionDto> = emptyList()

    private var waitForResponses = false
    private var latch = CompletableDeferred<Unit>()

    var getLinesCount = 0
    var getRoutesCount = 0
    var getStopsCount = 0
    var getArrivalsCount = 0
    var getPathCount = 0
    var getPathUpdatesOnlyCount = 0
    var getBusesCount = 0


    val getLinesCalled: Boolean
        get() = getLinesCount > 0

    val getRoutesCalled: Boolean
        get() = getRoutesCount > 0

    val getStopsCalled: Boolean
        get() = getStopsCount > 0

    val getArrivalsCalled: Boolean
        get() = getArrivalsCount > 0

    val getPathCalled: Boolean
        get() = getPathCount > 0

    val getPathUpdatesOnlyCalled: Boolean
        get() = getPathUpdatesOnlyCount > 0

    val getBusesCalled: Boolean
        get() = getBusesCount > 0

    fun waitForResponses(enable: Boolean) {
        waitForResponses = enable
        if (!enable) {
            latch.complete(Unit)
            latch = CompletableDeferred()
        }
    }

    private suspend fun awaitLatch() {
        if (waitForResponses) {
            latch.await()
        }
    }

    override suspend fun getLines(): List<LineDto> = withContext(Dispatchers.IO) {
        getLinesCount++
        awaitLatch()
        return@withContext linesResponse
    }

    override suspend fun getRoutes(): List<RouteDto> = withContext(Dispatchers.IO) {
        getRoutesCount++
        awaitLatch()
        return@withContext routesResponse
    }

    override suspend fun getStops(): List<StopDto> = withContext(Dispatchers.IO) {
        getStopsCount++
        awaitLatch()
        return@withContext stopsResponse
    }

    override suspend fun getArrivals(stop: StopId): List<BusArrivalDto> = withContext(Dispatchers.IO) {
        getArrivalsCount++
        awaitLatch()
        return@withContext arrivalsResponse
    }

    override suspend fun getPath(route: RouteId): PathDto = withContext(Dispatchers.IO) {
        getPathCount++
        awaitLatch()
        return@withContext pathResponse!!
    }

    override suspend fun getPaths(): List<PathDto> {
        awaitLatch()
        return pathsResponse
    }

    override suspend fun getBuses(route: RouteId): List<BusDto> = withContext(Dispatchers.IO) {
        getBusesCount++
        awaitLatch()
        return@withContext busesResponse
    }

    override suspend fun getCardInfo(card: CardId): CardInfoDto = withContext(Dispatchers.IO) {
        awaitLatch()
        return@withContext cardInfoResponse!!
    }

    override suspend fun getCardTransactions(card: CardId): List<CardTransactionDto> = withContext(Dispatchers.IO) {
        awaitLatch()
        return@withContext cardTransactionsResponse
    }
}
