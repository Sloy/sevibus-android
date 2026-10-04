package com.sloy.debugmenu.network

import com.sloy.debugmenu.testing.RecordingOverlayLogger
import com.sloy.debugmenu.testing.testContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.Cache
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkToolsViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(
        httpCache: Cache? = null,
        healthCheck: (suspend () -> Map<String, String>)? = null,
    ) = NetworkDebugModuleViewModel(
        dataSource = NetworkDebugModuleDataSource(testContext()),
        overlayLogger = RecordingOverlayLogger(),
        hostPresets = emptyList(),
        httpCache = httpCache,
        healthCheck = healthCheck,
        ioDispatcher = testDispatcher,
    )

    @Test
    fun `http cache is unavailable when no cache is provided`() {
        val viewModel = viewModel()

        expectThat(viewModel.toolsState.value.httpCache).isEqualTo(HttpCacheState.Unavailable)
    }

    @Test
    fun `http cache size is loaded on start`() {
        val viewModel = viewModel(httpCache = Cache(tempFolder.newFolder(), 1024))

        expectThat(viewModel.toolsState.value.httpCache).isEqualTo(HttpCacheState.Ready(sizeBytes = 0))
    }

    @Test
    fun `clearing http cache marks it as cleared`() {
        val viewModel = viewModel(httpCache = Cache(tempFolder.newFolder(), 1024))

        viewModel.onClearHttpCacheClicked()

        expectThat(viewModel.toolsState.value.httpCache).isEqualTo(HttpCacheState.Ready(sizeBytes = 0, justCleared = true))
    }

    @Test
    fun `health check is unavailable when no check is provided`() {
        expectThat(viewModel().isHealthCheckAvailable).isFalse()
    }

    @Test
    fun `successful health check exposes its entries`() {
        val viewModel = viewModel(healthCheck = { mapOf("Host" to "prod-1") })

        viewModel.onHealthCheckClicked()

        expectThat(viewModel.isHealthCheckAvailable).isTrue()
        expectThat(viewModel.toolsState.value.healthCheck).isEqualTo(HealthCheckState.Success(mapOf("Host" to "prod-1")))
    }

    @Test
    fun `failed health check exposes the error`() {
        val viewModel = viewModel(healthCheck = { error("boom") })

        viewModel.onHealthCheckClicked()

        expectThat(viewModel.toolsState.value.healthCheck).isA<HealthCheckState.Error>()
            .get { message }.isEqualTo("java.lang.IllegalStateException: boom")
    }

    @Test
    fun `bytes are formatted with readable units`() {
        expectThat(formatBytes(512)).isEqualTo("512 B")
        expectThat(formatBytes(2048)).isEqualTo("2.0 KB")
        expectThat(formatBytes(2_621_440)).isEqualTo("2.5 MB")
    }
}
