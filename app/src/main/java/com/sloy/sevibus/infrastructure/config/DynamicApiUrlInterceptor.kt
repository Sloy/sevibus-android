package com.sloy.sevibus.infrastructure.config

import okhttp3.Interceptor
import okhttp3.Response

class DynamicApiUrlInterceptor(
    private val configurationManager: ApiConfigurationManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val currentApiUrl = configurationManager.getCurrentApiUrl()

        val baseUrl: String = originalRequest.url.toUrl().toString()
        val newUrl = baseUrl.replace("https://base.url/", currentApiUrl)

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}