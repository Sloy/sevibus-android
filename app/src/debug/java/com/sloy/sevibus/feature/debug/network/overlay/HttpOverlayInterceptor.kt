package com.sloy.sevibus.feature.debug.network.overlay

import com.sloy.debugmenu.overlay.OverlayLoggerStateHolder
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class HttpOverlayInterceptor(private val overlayLoggerStateHolder: OverlayLoggerStateHolder) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val item = HttpOverlayItem(
            method = request.method,
            endpoint = request.url.encodedPath
        )
        overlayLoggerStateHolder.put(item)
        try {
            val response = chain.proceed(request)
            val cache = if (response.cacheResponse != null && response.networkResponse == null) {
                HttpOverlayItem.Cache.LOCAL
            } else if (response.cacheResponse != null && response.networkResponse?.code == 304) {
                HttpOverlayItem.Cache.NOT_MODIFIED
            } else {
                HttpOverlayItem.Cache.MISS
            }
            val status = response.networkResponse?.code ?: response.code
            overlayLoggerStateHolder.put(item.copy(status = status, cache = cache))
            return response
        } catch (e: IOException) {
            overlayLoggerStateHolder.put(item.copy(status = 999, error = e.message))
            throw e
        }
    }
}
