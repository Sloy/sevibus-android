package com.sloy.sevibus.data.repository

import com.sloy.sevibus.data.api.FakeSevibusApi
import com.sloy.sevibus.data.api.model.PathChecksumRequestDto
import com.sloy.sevibus.data.api.model.PathDto
import com.sloy.sevibus.data.api.model.PositionDto
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
        // Setup line repository with test data
        lineRepository.setLines(listOf(
            Line(
                id = 1,
                label = "1",
                description = "Line 1",
                color = LineColor.Red,
                group = "Test",
                routes = emptyList()
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
    fun `should obtain multiple paths successfully`() = runTest {
        api.pathResponse = pathDto("1.1", "checksum1")

        val result = repository.obtainPaths(listOf("1.1", "1.2"))

        expectThat(result).hasSize(2) // Both succeed since FakeSevibusApi returns same response for any route ID
        expectThat(result[0].routeId).isEqualTo("1.1")
        expectThat(result[1].routeId).isEqualTo("1.1") // FakeSevibusApi returns same response for all route IDs
    }

    @Test
    fun `should handle failures gracefully in obtainPaths`() = runTest {
        api.pathResponse = null // This will cause an exception

        val result = repository.obtainPaths(listOf("1.1", "1.2"))

        expectThat(result).hasSize(0) // All should fail gracefully
    }

    // REFRESH LOGIC TESTS - Testing the core refreshLocalData functionality

    @Test
    fun `should not call updatesOnly API when no cached paths exist`() = runTest {
        api.pathResponse = pathDto("1.1", "checksum1")
        
        repository.obtainPath("1.1")
        
        // Should NOT call updatesOnly API since obtainPath doesn't trigger refresh
        expectThat(api.getPathUpdatesOnlyCalled).isFalse()
        // Should call individual path API
        expectThat(api.getPathCalled).isTrue()
    }

    @Test
    fun `refresh should send correct checksums and handle empty response`() = runTest {
        // Setup existing cached paths
        dao.putPath(pathEntity("1.1", "checksum1"))
        dao.putPath(pathEntity("1.2", "checksum2"))
        
        // Create capturing API to verify the request
        val capturedRequests = mutableListOf<List<PathChecksumRequestDto>>()
        val capturingApi = object : FakeSevibusApi() {
            override suspend fun getPathUpdatesOnly(pathChecksums: List<PathChecksumRequestDto>): List<PathDto> {
                capturedRequests.add(pathChecksums)
                return emptyList() // No updates
            }
        }
        
        // Create repository with autoUpdate=true to trigger refresh in init
        val testRepository = RemoteAndLocalPathRepository(capturingApi, dao, lineRepository, shouldAutoUpdate = true)
        
        // Wait for async refresh to complete
        kotlinx.coroutines.delay(REFRESH_DELAY)
        
        // Verify the updatesOnly API was called with correct checksums
        expectThat(capturedRequests).hasSize(1)
        val request = capturedRequests[0]
        expectThat(request).hasSize(2)
        
        val request1 = request.find { it.routeId == "1.1" }!!
        val request2 = request.find { it.routeId == "1.2" }!!
        expectThat(request1.checksum).isEqualTo("checksum1")
        expectThat(request2.checksum).isEqualTo("checksum2")
        
        // Verify database wasn't changed (no updates returned)
        expectThat(dao.getPath("1.1")).isNotNull().get { checksum }.isEqualTo("checksum1")
        expectThat(dao.getPath("1.2")).isNotNull().get { checksum }.isEqualTo("checksum2")
    }

    @Test
    fun `refresh should update database with changed paths`() = runTest {
        // Setup existing cached paths with old checksums
        dao.putPath(pathEntity("1.1", "oldChecksum1"))
        dao.putPath(pathEntity("1.2", "oldChecksum2"))
        dao.putPath(pathEntity("1.3", "oldChecksum3"))
        
        // Setup API to return updated paths (only 1.1 and 1.3 changed)
        val capturedRequests = mutableListOf<List<PathChecksumRequestDto>>()
        val capturingApi = object : FakeSevibusApi() {
            override suspend fun getPathUpdatesOnly(pathChecksums: List<PathChecksumRequestDto>): List<PathDto> {
                capturedRequests.add(pathChecksums)
                return listOf(
                    pathDto("1.1", "newChecksum1"),
                    pathDto("1.3", "newChecksum3")
                )
            }
        }
        
        // Create repository with autoUpdate=true to trigger refresh in init
        val testRepository = RemoteAndLocalPathRepository(capturingApi, dao, lineRepository, shouldAutoUpdate = true)
        
        // Wait for async refresh to complete
        kotlinx.coroutines.delay(REFRESH_DELAY)
        
        // Verify correct request was sent
        expectThat(capturedRequests).hasSize(1)
        expectThat(capturedRequests[0]).hasSize(3)
        
        // Verify database was updated correctly
        expectThat(dao.getPath("1.1")).isNotNull().get { checksum }.isEqualTo("newChecksum1") // Updated
        expectThat(dao.getPath("1.2")).isNotNull().get { checksum }.isEqualTo("oldChecksum2") // Unchanged
        expectThat(dao.getPath("1.3")).isNotNull().get { checksum }.isEqualTo("newChecksum3") // Updated
        
        // Now test that individual path fetching still works
        capturingApi.pathResponse = pathDto("1.4", "checksum4")
        testRepository.obtainPath("1.4")
        expectThat(dao.getPath("1.4")).isNotNull().get { checksum }.isEqualTo("checksum4")   // New from individual call
    }

    @Test
    fun `refresh should handle new paths from API`() = runTest {
        // Setup existing cached paths
        dao.putPath(pathEntity("1.1", "checksum1"))
        
        // Setup API to return existing + new paths
        val capturingApi = object : FakeSevibusApi() {
            override suspend fun getPathUpdatesOnly(pathChecksums: List<PathChecksumRequestDto>): List<PathDto> {
                return listOf(
                    pathDto("1.1", "checksum1"), // Unchanged (shouldn't happen in practice but let's handle it)
                    pathDto("1.2", "checksum2"), // New path
                    pathDto("1.3", "checksum3")  // Another new path
                )
            }
        }
        
        // Create repository with autoUpdate=true to trigger refresh in init
        val testRepository = RemoteAndLocalPathRepository(capturingApi, dao, lineRepository, shouldAutoUpdate = true)
        
        // Wait for async refresh to complete
        kotlinx.coroutines.delay(REFRESH_DELAY)
        
        // Verify all paths are now in database
        expectThat(dao.getPath("1.1")).isNotNull().get { checksum }.isEqualTo("checksum1")
        expectThat(dao.getPath("1.2")).isNotNull().get { checksum }.isEqualTo("checksum2") // New
        expectThat(dao.getPath("1.3")).isNotNull().get { checksum }.isEqualTo("checksum3") // New
        
        // Test individual path fetching still works
        capturingApi.pathResponse = pathDto("1.4", "checksum4")
        testRepository.obtainPath("1.4")
        expectThat(dao.getPath("1.4")).isNotNull().get { checksum }.isEqualTo("checksum4") // From individual call
    }

    @Test
    fun `refresh should handle API errors gracefully`() = runTest {
        // Setup existing cached paths
        dao.putPath(pathEntity("1.1", "oldChecksum"))
        
        // Setup API to throw an error for updatesOnly
        val failingApi = object : FakeSevibusApi() {
            override suspend fun getPathUpdatesOnly(pathChecksums: List<PathChecksumRequestDto>): List<PathDto> {
                throw RuntimeException("Network error")
            }
        }
        
        // Create repository with autoUpdate=true to trigger refresh in init (which will fail)
        val testRepository = RemoteAndLocalPathRepository(failingApi, dao, lineRepository, shouldAutoUpdate = true)
        
        // Wait for failed refresh attempt
        kotlinx.coroutines.delay(REFRESH_DELAY)
        
        // Individual path fetch should still succeed even though refresh failed
        failingApi.pathResponse = pathDto("1.2", "checksum2")
        val result = testRepository.obtainPath("1.2")
        
        expectThat(result.routeId).isEqualTo("1.2")
        
        // Original path should be unchanged (refresh failed)
        expectThat(dao.getPath("1.1")).isNotNull().get { checksum }.isEqualTo("oldChecksum")
        expectThat(dao.getPath("1.2")).isNotNull().get { checksum }.isEqualTo("checksum2")
    }

    @Test
    fun `should not call updatesOnly API when no cached paths exist during initialization`() = runTest {
        // Start with empty database - no cached paths
        
        val trackingApi = object : FakeSevibusApi() {
            override suspend fun getPathUpdatesOnly(pathChecksums: List<PathChecksumRequestDto>): List<PathDto> {
                return emptyList() // No updates
            }
        }
        
        // Create repository with autoUpdate=true - should not trigger refresh since no cached paths
        val testRepository = RemoteAndLocalPathRepository(trackingApi, dao, lineRepository, shouldAutoUpdate = true)
        
        // Wait for any potential async operations
        kotlinx.coroutines.delay(REFRESH_DELAY)
        
        // Should not have called updatesOnly since no cached paths exist
        expectThat(trackingApi.getPathUpdatesOnlyCount).isEqualTo(0)
        
        // Individual path fetching should work normally
        trackingApi.pathResponse = pathDto("1.1", "checksum1")
        testRepository.obtainPath("1.1")
        
        // Should still be 0 calls to updatesOnly
        expectThat(trackingApi.getPathUpdatesOnlyCount).isEqualTo(0)
        expectThat(trackingApi.getPathCalled).isTrue() // Individual call was made
    }

    private fun pathDto(routeId: String, checksum: String) = PathDto(
        routeId = routeId,
        polyline = "_p~iF~ps|U_ulLnnqC" as Polyline, // Encoded polyline for coordinates (1.0,1.0) -> (2.0,2.0) 
        checksum = checksum
    )

    private fun pathEntity(routeId: String, checksum: String) = PathEntity(
        routeId = routeId,
        points = listOf(
            PositionDto(1.0, 1.0),
            PositionDto(2.0, 2.0)
        ),
        checksum = checksum
    )
}