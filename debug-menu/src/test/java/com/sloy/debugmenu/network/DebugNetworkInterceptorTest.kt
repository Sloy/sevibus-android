package com.sloy.debugmenu.network

import com.sloy.debugmenu.testing.RecordingOverlayLogger
import com.sloy.debugmenu.testing.testContext
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.containsExactly
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isGreaterThanOrEqualTo
import strikt.assertions.isLessThanOrEqualTo
import strikt.assertions.isTrue
import strikt.assertions.single
import java.io.IOException
import kotlin.random.Random

class DebugNetworkInterceptorTest {

    private val store = NetworkDebugModuleDataSource(testContext())
    private val overlayLogger = RecordingOverlayLogger()
    private val sleeps = mutableListOf<Long>()
    private val interceptor = DebugNetworkInterceptor(
        store = store,
        overlayLogger = overlayLogger,
        randomId = { "id-1" },
        sleep = { sleeps += it },
        random = Random(7),
    )
    private val request = Request.Builder().url("https://app.example.com/api/stops?x=1").build()

    @Test
    fun `reports pending and finished request when overlay is enabled`() {
        val chain = chainReturning(200)

        val response = interceptor.intercept(chain)

        expectThat(response.code).isEqualTo(200)
        expectThat(overlayLogger.putItems).containsExactly(
            HttpOverlayLoggerItem(method = "GET", endpoint = "/api/stops", id = "id-1"),
            HttpOverlayLoggerItem(method = "GET", endpoint = "/api/stops", id = "id-1", status = 200, cache = HttpOverlayLoggerItem.Cache.MISS),
        )
    }

    @Test
    fun `does not report when overlay is disabled`() {
        store.updateState(NetworkDebugModuleState(isHttpOverlayEnabled = false))

        interceptor.intercept(chainReturning(200))

        expectThat(overlayLogger.putItems).isEmpty()
    }

    @Test
    fun `does not sleep when latency is off`() {
        interceptor.intercept(chainReturning(200))

        expectThat(sleeps).containsExactly(0L)
    }

    @Test
    fun `sleeps with jittered latency`() {
        store.updateState(NetworkDebugModuleState(latencyPreset = LatencyPreset.Slow3G))

        interceptor.intercept(chainReturning(200))

        expectThat(sleeps).single().isGreaterThanOrEqualTo(1800L).isLessThanOrEqualTo(2200L)
    }

    @Test
    fun `forced failure returns 500 without hitting the network`() {
        store.updateState(NetworkDebugModuleState(isForceFailureEnabled = true))
        val chain = chainReturning(200)

        val response = interceptor.intercept(chain)

        expectThat(response.code).isEqualTo(500)
        expectThat(response.body?.string()).isEqualTo("""{"error":"Forced failure (debug)"}""")
        verify(chain, never()).proceed(any())
        expectThat(overlayLogger.putItems).hasSize(2)
        expectThat(overlayLogger.putItems.last()).isEqualTo(
            HttpOverlayLoggerItem(method = "GET", endpoint = "/api/stops", id = "id-1", status = 500, cache = HttpOverlayLoggerItem.Cache.MISS)
        )
    }

    @Test
    fun `forced failure with auto reset disables itself`() {
        store.updateState(NetworkDebugModuleState(isForceFailureEnabled = true, autoResetForceFailure = true))

        interceptor.intercept(chainReturning(200))

        expectThat(store.getCurrentState().isForceFailureEnabled).isFalse()
    }

    @Test
    fun `forced failure without auto reset stays enabled`() {
        store.updateState(NetworkDebugModuleState(isForceFailureEnabled = true))

        interceptor.intercept(chainReturning(200))

        expectThat(store.getCurrentState().isForceFailureEnabled).isTrue()
    }

    @Test
    fun `host override replaces scheme host and port keeping path and query`() {
        store.updateState(NetworkDebugModuleState(hostOverride = "http://192.168.1.10:8080"))
        val chain = chainReturning(200)

        interceptor.intercept(chain)

        val captor = argumentCaptor<Request>()
        verify(chain).proceed(captor.capture())
        expectThat(captor.firstValue.url.toString()).isEqualTo("http://192.168.1.10:8080/api/stops?x=1")
    }

    @Test
    fun `default host is used when there is no override`() {
        val withDefault = DebugNetworkInterceptor(
            store = store,
            overlayLogger = overlayLogger,
            randomId = { "id-1" },
            sleep = { sleeps += it },
            random = Random(7),
            defaultHost = HostPreset("Dev", "https://dev.example.com"),
        )
        val chain = chainReturning(200)

        withDefault.intercept(chain)

        val captor = argumentCaptor<Request>()
        verify(chain).proceed(captor.capture())
        expectThat(captor.firstValue.url.toString()).isEqualTo("https://dev.example.com/api/stops?x=1")
    }

    @Test
    fun `invalid host override is ignored`() {
        store.updateState(NetworkDebugModuleState(hostOverride = "not a url"))
        val chain = chainReturning(200)

        interceptor.intercept(chain)

        val captor = argumentCaptor<Request>()
        verify(chain).proceed(captor.capture())
        expectThat(captor.firstValue.url.toString()).isEqualTo("https://app.example.com/api/stops?x=1")
    }

    @Test
    fun `io errors are reported and rethrown`() {
        val chain = mock<Interceptor.Chain> {
            on { request() } doReturn request
            on { proceed(any()) } doThrow IOException("timeout")
        }

        expectThrows<IOException> { interceptor.intercept(chain) }

        expectThat(overlayLogger.putItems.last()).isEqualTo(
            HttpOverlayLoggerItem(
                method = "GET",
                endpoint = "/api/stops",
                id = "id-1",
                status = HttpOverlayLoggerItem.STATUS_IO_EXCEPTION,
                error = "timeout",
            )
        )
    }

    @Test
    fun `unexpected errors are reported and rethrown`() {
        val chain = mock<Interceptor.Chain> {
            on { request() } doReturn request
            on { proceed(any()) } doThrow IllegalStateException("boom")
        }

        expectThrows<IllegalStateException> { interceptor.intercept(chain) }

        expectThat(overlayLogger.putItems.last()).isEqualTo(
            HttpOverlayLoggerItem(
                method = "GET",
                endpoint = "/api/stops",
                id = "id-1",
                status = HttpOverlayLoggerItem.STATUS_IO_EXCEPTION,
                error = "boom",
            )
        )
    }

    private fun chainReturning(code: Int): Interceptor.Chain = mock {
        on { request() } doReturn request
        on { proceed(any()) } doAnswer { invocation ->
            Response.Builder()
                .request(invocation.getArgument(0))
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message("OK")
                .body("".toResponseBody())
                .build()
        }
    }
}
