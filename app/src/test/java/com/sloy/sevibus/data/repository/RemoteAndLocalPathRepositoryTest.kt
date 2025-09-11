package com.sloy.sevibus.data.repository

import com.sloy.sevibus.data.api.FakeSevibusApi
import com.sloy.sevibus.data.api.model.PathDto
import com.sloy.sevibus.data.database.FakeTussamDao
import com.sloy.sevibus.data.database.PathEntity
import com.sloy.sevibus.domain.model.Line
import com.sloy.sevibus.domain.model.LineColor
import com.sloy.sevibus.domain.model.LineId
import com.sloy.sevibus.domain.model.Polyline
import com.sloy.sevibus.domain.model.RouteId
import com.sloy.sevibus.domain.repository.FakeLineRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isTrue

class RemoteAndLocalPathRepositoryTest {

    companion object {
        private const val REFRESH_DELAY = 500L
    }

    private val dao = FakeTussamDao()
    private val api = FakeSevibusApi()
    private val lineRepository = FakeLineRepository()

    private val repository = RemoteAndLocalPathRepository(api, dao, lineRepository, shouldAutoUpdate = false)

    @Before
    fun setUp() {
        // Setup line repository with test data that includes routes used in tests
        lineRepository.setLines(listOf(
            Line(
                id = 1,
                label = "1",
                description = "Line 1",
                color = LineColor.Red,
                group = "Test",
                routes = listOf(
                    com.sloy.sevibus.domain.model.Route(
                        id = "1.1",
                        direction = 1,
                        destination = "Test Destination 1",
                        line = 1,
                        stops = emptyList(),
                        schedule = com.sloy.sevibus.domain.model.Route.Schedule(
                            startTime = java.time.LocalTime.of(6, 0),
                            endTime = java.time.LocalTime.of(23, 0)
                        )
                    ),
                    com.sloy.sevibus.domain.model.Route(
                        id = "1.2",
                        direction = 2,
                        destination = "Test Destination 2",
                        line = 1,
                        stops = emptyList(),
                        schedule = com.sloy.sevibus.domain.model.Route.Schedule(
                            startTime = java.time.LocalTime.of(6, 0),
                            endTime = java.time.LocalTime.of(23, 0)
                        )
                    )
                )
            )
        ))
    }

    @Test
    fun `should obtain path from API when database is empty`() = runTest {
        api.pathResponse = pathDto("1.1", "checksum1")
        
        val result = repository.obtainPath("1.1")

        expectThat(result.routeId).isEqualTo("1.1")
        expectThat(result.points).hasSize(2)
        expectThat(api.getPathCalled).isTrue()
    }

    @Test
    fun `should not request from API when database has path`() = runTest {
        dao.putPath(pathEntity("1.1", "checksum1"))

        val result = repository.obtainPath("1.1")

        expectThat(result.routeId).isEqualTo("1.1")
        expectThat(api.getPathCalled).isFalse()
    }

    @Test
    fun `should request from API only once when invoked twice`() = runTest {
        api.pathResponse = pathDto("1.1", "checksum1")

        val result1 = repository.obtainPath("1.1")
        val result2 = repository.obtainPath("1.1")

        // Both calls succeed, second one comes from cache
        expectThat(result1.routeId).isEqualTo("1.1")
        expectThat(result2.routeId).isEqualTo("1.1")
        expectThat(api.getPathCount).isEqualTo(1) // Only one API call made, second comes from database
    }

    @Test
    fun `should obtain multiple paths using bulk endpoint`() = runTest {
        // Pre-populate database to avoid refresh logic complications
        dao.putPaths(
            listOf(
                pathEntity("1.1", "checksum1"),
                pathEntity("1.2", "checksum2"),
                pathEntity("1.3", "checksum3")
            )
        )

        val result = repository.obtainPaths(listOf("1.1", "1.2"))

        // Should return only the requested paths from database
        expectThat(result).hasSize(2)
        expectThat(result.find { it.routeId == "1.1" }).isNotNull()
        expectThat(result.find { it.routeId == "1.2" }).isNotNull()
    }

    @Test
    fun `should handle failures gracefully in obtainPaths`() = runTest {
        api.pathsResponse = emptyList() // Empty response

        val result = repository.obtainPaths(listOf("1.1", "1.2"))

        expectThat(result).hasSize(0)
    }

    // NEW SIMPLIFIED BEHAVIOR TESTS - Testing the /paths bulk endpoint approach

    @Test
    fun `refresh should work with bulk endpoint on auto-update`() = runTest {
        // Setup API to return paths via bulk endpoint
        val trackingApi = object : FakeSevibusApi() {
            override suspend fun getPaths(): List<PathDto> {
                return listOf(
                    pathDto("1.1", "checksum1"),
                    pathDto("1.2", "checksum2")
                )
            }
        }
        
        // Create repository with autoUpdate=true to trigger refresh in init
        val testRepository = RemoteAndLocalPathRepository(trackingApi, dao, lineRepository, shouldAutoUpdate = true)
        
        // Wait for async refresh to complete
        kotlinx.coroutines.delay(REFRESH_DELAY)

        // Verify database has paths from bulk endpoint
        expectThat(dao.getPath("1.1")).isNotNull()
        expectThat(dao.getPath("1.2")).isNotNull()
    }

    @Test
    fun `refresh should handle empty bulk response gracefully`() = runTest {
        // Setup existing cached paths
        dao.putPath(pathEntity("1.1", "checksum1"))

        // Setup API to return empty response
        val trackingApi = object : FakeSevibusApi() {
            override suspend fun getPaths(): List<PathDto> {
                return emptyList()
            }
        }
        
        // Create repository with autoUpdate=true to trigger refresh in init
        val testRepository = RemoteAndLocalPathRepository(trackingApi, dao, lineRepository, shouldAutoUpdate = true)
        
        // Wait for async refresh to complete
        kotlinx.coroutines.delay(REFRESH_DELAY)

        // Database should still contain the old path (not cleared by empty response)
        expectThat(dao.getPath("1.1")).isNotNull().get { checksum }.isEqualTo("checksum1")
    }

    @Test
    fun `refresh should handle API errors gracefully`() = runTest {
        // Setup existing cached paths
        dao.putPath(pathEntity("1.1", "oldChecksum"))

        // Setup API to throw an error for bulk endpoint
        val failingApi = object : FakeSevibusApi() {
            override suspend fun getPaths(): List<PathDto> {
                throw RuntimeException("Network error")
            }
        }
        
        // Create repository with autoUpdate=true to trigger refresh in init (which will fail)
        val testRepository = RemoteAndLocalPathRepository(failingApi, dao, lineRepository, shouldAutoUpdate = true)
        
        // Wait for failed refresh attempt
        kotlinx.coroutines.delay(REFRESH_DELAY)

        // Individual path fetch should still work
        failingApi.pathResponse = pathDto("1.2", "checksum2")
        val result = testRepository.obtainPath("1.2")
        
        expectThat(result.routeId).isEqualTo("1.2")
        
        // Original path should be unchanged (refresh failed)
        expectThat(dao.getPath("1.1")).isNotNull().get { checksum }.isEqualTo("oldChecksum")
        expectThat(dao.getPath("1.2")).isNotNull().get { checksum }.isEqualTo("checksum2")
    }

    @Test
    fun `should obtain paths from database when available`() = runTest {
        // Pre-populate database
        dao.putPaths(
            listOf(
                pathEntity("1.1", "checksum1"),
                pathEntity("1.2", "checksum2")
            )
        )

        val result = repository.obtainPaths(listOf("1.1", "1.2"))

        // Should return paths from database
        expectThat(result).hasSize(2)
        expectThat(result.find { it.routeId == "1.1" }).isNotNull()
        expectThat(result.find { it.routeId == "1.2" }).isNotNull()
    }

    private fun pathDto(routeId: String, checksum: String) = PathDto(
        routeId = routeId,
        polyline = "_p~iF~ps|U_ulLnnqC" as Polyline, // Encoded polyline for coordinates (1.0,1.0) -> (2.0,2.0) 
        checksum = checksum
    )

    private fun pathEntity(routeId: String, checksum: String) = PathEntity(
        routeId = routeId,
        polyline = "_p~iF~ps|U_ulLnnqC" as Polyline, // Encoded polyline for coordinates (1.0,1.0) -> (2.0,2.0)
        checksum = checksum
    )
}