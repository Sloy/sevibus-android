package com.sloy.sevibus.feature.debug.http

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class HttpOverlayInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val item = HttpOverlayItem(
            method = request.method,
            endpoint = request.url.encodedPath
        )
        HttpOverlayState.put(item)
        try {
            val response = chain.proceed(request)
            HttpOverlayState.put(item.copy(status = response.code))
            return response
        } catch (e: IOException) {
            HttpOverlayState.put(item.copy(status = 999))
            throw e
        }
    }
}
