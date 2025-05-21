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

class RemoteAndLocalLineRepositoryTest {

    val dao = FakeTussamDao()
    val api = FakeSevibusApi()
    val routeRepository = FakeRouteRepository()

    val repository = RemoteAndLocalLineRepository(api, dao, routeRepository, shouldAutoUpdate = false)

    @Before
    fun setUp() {
        api.linesResponse = listOf(
            lineDto(1),
            lineDto(2)
        )
        api.routesResponse = listOf(
            routeDto("1.1"),
            routeDto("1.2"),
        )
    }

    @Test
    fun `should obtain lines from API when Database is empty`() = runTest {
        val result = repository.obtainLines()

        expectThat(result).hasSize(2)
        expectThat(api.getLinesCalled).isTrue()
    }

    @Test
    fun `should obtain lines by ID from API when Database is empty`() = runTest {
        val result = repository.obtainLines(listOf(1))

        expectThat(result).hasSize(1)
        expectThat(api.getLinesCalled).isTrue()
    }

    @Test
    fun `should not request from API when Database is not empty`() = runTest {
        dao.putLines(listOf(lineEntity(1), lineEntity(2)))

        val result = repository.obtainLines()

        expectThat(result).hasSize(2)
        expectThat(api.getLinesCalled).isFalse()
    }

    @Test
    fun `should request from API only once when invoked twice`() = runTest {
        api.waitForResponses(true)

        val job1 = async { repository.obtainLines() }
        val job2 = async { repository.obtainLines() }


        api.waitForResponses(false)
        job1.await()
        job2.await()

        expectThat(api.getLinesCount).isEqualTo(1)
    }

    private fun routeDto(id: RouteId) =
        RouteDto(id, direction = id.direction, "route $id", line = id.lineId, stops = emptyList(), schedule = RouteDto.ScheduleDto("06:10", "07:20"))

    private fun lineDto(id: LineId) =
        LineDto(label = "$id", id = id, description = "Line $id", color = LineColor.Red, group = "", routes = emptyList())

    private fun lineEntity(id: LineId) =
        LineEntity(label = "$id", id = id, description = "Line $id", color = LineColor.Red, group = "", routes = emptyList())
}
