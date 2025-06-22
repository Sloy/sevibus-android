package com.sloy.sevibus.infrastructure.config

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response

class DynamicApiUrlInterceptor(
    private val configurationManager: ApiConfigurationManager
) : Interceptor {

    companion object {
        const val BASE_URL_PLACEHOLDER = "https://base.url/"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val currentApiUrl = configurationManager.getCurrentApiUrl()

        // Only replace if URL matches our placeholder
        val originalUrl = originalRequest.url.toString()
        if (!originalUrl.startsWith(BASE_URL_PLACEHOLDER)) {
            return chain.proceed(originalRequest)
        }

        // Parse the target API URL
        val targetUrl = currentApiUrl.toHttpUrl()

        // Extract the path after our placeholder  
        val pathAfterPlaceholder = originalUrl.removePrefix(BASE_URL_PLACEHOLDER)
        
        // Split path from query/fragment
        val pathPart = if (pathAfterPlaceholder.contains('?')) {
            pathAfterPlaceholder.substringBefore('?')
        } else {
            pathAfterPlaceholder
        }

        // Build new URL: target URL + original path + query + fragment
        val newUrl = targetUrl.newBuilder()
            .addPathSegments(pathPart.trimStart('/'))
            .query(originalRequest.url.query)
            .fragment(originalRequest.url.fragment)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}