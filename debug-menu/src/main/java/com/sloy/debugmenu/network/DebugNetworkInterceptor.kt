package com.sloy.debugmenu.network

import com.sloy.debugmenu.overlay.OverlayLogger
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.InterruptedIOException
import java.util.UUID
import kotlin.random.Random

/**
 * Applies the network debug settings: HTTP overlay, host override, latency and forced failures.
 *
 * Add it only to OkHttp clients that talk to your API, since the host override rewrites every request.
 */
class DebugNetworkInterceptor internal constructor(
    private val store: NetworkDebugModuleDataSource,
    private val overlayLogger: OverlayLogger,
    private val randomId: () -> String,
    private val sleep: (Long) -> Unit,
    private val random: Random,
) : Interceptor {

    constructor(store: NetworkDebugModuleDataSource, overlayLogger: OverlayLogger) : this(
        store = store,
        overlayLogger = overlayLogger,
        randomId = { UUID.randomUUID().toString() },
        sleep = ::defaultSleep,
        random = Random.Default,
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val state = store.getCurrentState()
        val request = chain.request().withHostOverride(state.hostOverride)
        val overlayItem = HttpOverlayLoggerItem(method = request.method, endpoint = request.url.encodedPath, id = randomId())
        report(overlayItem)
        try {
            sleep(state.latencyPreset.nextDelayMs(random))
            if (state.isForceFailureEnabled) {
                return forceFailure(request, overlayItem)
            }
            return proceedAndReport(chain, request, overlayItem)
        } catch (e: Throwable) {
            report(
                overlayItem.copy(
                    status = HttpOverlayLoggerItem.STATUS_IO_EXCEPTION,
                    error = e.message ?: e::class.simpleName ?: "Error",
                )
            )
            throw e
        }
    }

    private fun forceFailure(request: Request, overlayItem: HttpOverlayLoggerItem): Response {
        val current = store.getCurrentState()
        if (current.autoResetForceFailure) {
            store.updateState(current.copy(isForceFailureEnabled = false))
        }
        report(overlayItem.copy(status = FORCED_FAILURE_CODE, cache = HttpOverlayLoggerItem.Cache.MISS))
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(FORCED_FAILURE_CODE)
            .message(FORCED_FAILURE_MESSAGE)
            .body(FORCED_FAILURE_BODY.toResponseBody(JSON_MEDIA_TYPE.toMediaType()))
            .build()
    }

    private fun proceedAndReport(chain: Interceptor.Chain, request: Request, overlayItem: HttpOverlayLoggerItem): Response {
        val response = chain.proceed(request)
        report(overlayItem.copy(status = response.code, cache = response.cacheClass()))
        return response
    }

    private fun report(item: HttpOverlayLoggerItem) {
        if (store.getCurrentState().isHttpOverlayEnabled) {
            overlayLogger.put(item)
        }
    }

    private fun Response.cacheClass(): HttpOverlayLoggerItem.Cache = when {
        cacheResponse != null && networkResponse == null -> HttpOverlayLoggerItem.Cache.LOCAL
        cacheResponse != null && networkResponse?.code == HTTP_NOT_MODIFIED -> HttpOverlayLoggerItem.Cache.NOT_MODIFIED
        else -> HttpOverlayLoggerItem.Cache.MISS
    }
}

internal fun Request.withHostOverride(hostOverride: String?): Request {
    val target = hostOverride?.toHttpUrlOrNull() ?: return this
    val newUrl = url.newBuilder()
        .scheme(target.scheme)
        .host(target.host)
        .port(target.port)
        .build()
    return newBuilder().url(newUrl).build()
}

private fun defaultSleep(millis: Long) {
    if (millis <= 0L) return
    try {
        Thread.sleep(millis)
    } catch (e: InterruptedException) {
        Thread.currentThread().interrupt()
        throw InterruptedIOException("Network debug latency simulation interrupted").apply { initCause(e) }
    }
}

private const val FORCED_FAILURE_CODE = 500
private const val HTTP_NOT_MODIFIED = 304
private const val FORCED_FAILURE_MESSAGE = "Forced failure (debug)"
private const val FORCED_FAILURE_BODY = """{"error":"Forced failure (debug)"}"""
private const val JSON_MEDIA_TYPE = "application/json"
