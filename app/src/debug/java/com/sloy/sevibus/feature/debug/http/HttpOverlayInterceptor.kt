package com.sloy.sevibus.feature.debug.http

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class HttpOverlayInterceptor(private val httpOverlayState: HttpOverlayState) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val item = HttpOverlayItem(
            method = request.method,
            endpoint = request.url.encodedPath
        )
        httpOverlayState.put(item)
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
            httpOverlayState.put(item.copy(status = status, cache = cache))
            return response
        } catch (e: IOException) {
            httpOverlayState.put(item.copy(status = 999, error = e.message))
            throw e
        }
    }
}
