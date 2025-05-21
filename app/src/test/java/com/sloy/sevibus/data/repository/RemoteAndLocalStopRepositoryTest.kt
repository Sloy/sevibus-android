package com.sloy.sevibus.data.repository

import com.sloy.sevibus.data.api.FakeSevibusApi
import com.sloy.sevibus.data.api.model.PositionDto
import com.sloy.sevibus.data.api.model.StopDto
import com.sloy.sevibus.data.database.FakeTussamDao
import com.sloy.sevibus.data.database.StopEntity
import com.sloy.sevibus.domain.model.Position
import com.sloy.sevibus.domain.repository.FakeLineRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue


class RemoteAndLocalStopRepositoryTest {

    val dao = FakeTussamDao()
    val api = FakeSevibusApi()
    val linesRepository = FakeLineRepository()

    val repository = RemoteAndLocalStopRepository(api, dao, linesRepository, shouldAutoUpdate = false)


    @Before
    fun setUp() {
        api.stopsResponse = listOf(
            StopDto(1, "Stop 1", PositionDto(1.0, 1.0), emptyList()),
            StopDto(2, "Stop 2", PositionDto(2.0, 2.0), emptyList()),
        )
    }

    @Test
    fun `should obtain stops from API when Database is empty`() = runTest {
        val result = repository.obtainStops()

        expectThat(result).hasSize(2)
        expectThat(api.getStopsCalled).isTrue()
    }

    @Test
    fun `should not request from API when Database is not empty`() = runTest {
        dao.putStops(
            listOf(
                StopEntity(1, "Stop 1", Position(1.0, 1.0), emptyList()),
                StopEntity(2, "Stop 2", Position(1.0, 1.0), emptyList()),
            )
        )

        val result = repository.obtainStops()

        expectThat(result).hasSize(2)
        expectThat(api.getStopsCalled).isFalse()
    }

    @Test
    fun `should request from API only once when invoked twice`() = runTest {
        api.waitForResponses(true)

        val job1 = async { repository.obtainStops() }
        val job2 = async { repository.obtainStops() }

        api.waitForResponses(false)
        job1.await()
        job2.await()

        expectThat(api.getStopsCount).isEqualTo(1)
    }
}
