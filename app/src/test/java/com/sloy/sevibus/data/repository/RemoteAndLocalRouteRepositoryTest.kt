package com.sloy.sevibus.data.repository

import com.sloy.sevibus.data.api.FakeSevibusApi
import com.sloy.sevibus.data.api.model.LineDto
import com.sloy.sevibus.data.api.model.RouteDto
import com.sloy.sevibus.data.database.FakeTussamDao
import com.sloy.sevibus.data.database.LineEntity
import com.sloy.sevibus.data.database.RouteEntity
import com.sloy.sevibus.domain.model.LineColor
import com.sloy.sevibus.domain.model.LineId
import com.sloy.sevibus.domain.model.Route
import com.sloy.sevibus.domain.model.RouteId
import com.sloy.sevibus.domain.model.direction
import com.sloy.sevibus.domain.model.lineId
import com.sloy.sevibus.domain.repository.FakeRouteRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue
import java.time.LocalTime


class RemoteAndLocalRouteRepositoryTest {

    val dao = FakeTussamDao()
    val api = FakeSevibusApi()

    val repository = RemoteAndLocalRouteRepository(api, dao, shouldAutoUpdate = false)

    @Before
    fun setUp() {
        api.routesResponse = listOf(
            routeDto("1.1"),
            routeDto("1.2"),
        )
    }

    @Test
    fun `should obtain routes from API when Database is empty`() = runTest {
        val result = repository.obtainRoutes()

        expectThat(result).hasSize(2)
        expectThat(api.getRoutesCalled).isTrue()
    }

    @Test
    fun `should not request from API when Database is not empty`() = runTest {
        dao.putRoutes(listOf(routeEntity("1.1"), routeEntity("1.2")))

        val result = repository.obtainRoutes()

        expectThat(result).hasSize(2)
        expectThat(api.getRoutesCalled).isFalse()
    }

    @Test
    fun `should request from API only once when invoked twice`() = runTest {
        api.waitForResponses(true)

        val job1 = async { repository.obtainRoutes() }
        val job2 = async { repository.obtainRoutes() }


        api.waitForResponses(false)
        job1.await()
        job2.await()

        expectThat(api.getRoutesCount).isEqualTo(1)
    }

    private fun routeDto(id: RouteId) =
        RouteDto(
            id,
            direction = id.direction,
            "route $id",
            line = id.lineId,
            stops = emptyList(),
            schedule = RouteDto.ScheduleDto("06:10", "07:20")
        )

    private fun routeEntity(id: RouteId) =
        RouteEntity(
            id,
            direction = id.direction,
            "route $id",
            line = id.lineId,
            stops = emptyList(),
            schedule = Route.Schedule(LocalTime.now(), LocalTime.now())
        )

}
