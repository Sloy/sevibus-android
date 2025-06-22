package com.sloy.sevibus.infrastructure.config

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class DynamicApiUrlInterceptorTest {

    private lateinit var apiConfigurationManager: ApiConfigurationManager
    private lateinit var interceptor: DynamicApiUrlInterceptor
    private lateinit var mockChain: Interceptor.Chain
    private lateinit var mockResponse: Response

    @Before
    fun setup() {
        apiConfigurationManager = ApiConfigurationManager()
        interceptor = DynamicApiUrlInterceptor(apiConfigurationManager)
        mockChain = mock()
        mockResponse = mock()
    }

    @Test
    fun `should replace base URL with configured API URL`() {
        val substituteUrl = "https://api.example.com/v1/"
        val originalUrl = "${DynamicApiUrlInterceptor.BASE_URL_PLACEHOLDER}api/"
        val expectedUrl = "https://api.example.com/v1/api/"

        testUrlReplacement(substituteUrl, originalUrl, expectedUrl)
    }

    @Test
    fun `should replace URL and preserve endpoint path`() {
        val substituteUrl = "https://api.example.com/v1/"
        val originalUrl = "${DynamicApiUrlInterceptor.BASE_URL_PLACEHOLDER}admin/health"
        val expectedUrl = "https://api.example.com/v1/admin/health"

        testUrlReplacement(substituteUrl, originalUrl, expectedUrl)
    }

    @Test
    fun `should replace URL and preserve nested endpoint paths`() {
        val substituteUrl = "https://api.example.com/v1/"
        val originalUrl = "${DynamicApiUrlInterceptor.BASE_URL_PLACEHOLDER}api/stops/123/arrivals"
        val expectedUrl = "https://api.example.com/v1/api/stops/123/arrivals"

        testUrlReplacement(substituteUrl, originalUrl, expectedUrl)
    }

    @Test
    fun `should preserve query parameters`() {
        val substituteUrl = "https://api.example.com/v1/"
        val originalUrl = "${DynamicApiUrlInterceptor.BASE_URL_PLACEHOLDER}api/search?q=test&limit=10"
        val expectedUrl = "https://api.example.com/v1/api/search?q=test&limit=10"

        testUrlReplacement(substituteUrl, originalUrl, expectedUrl)
    }

    @Test
    fun `should handle different configured URL schemes`() {
        val substituteUrl = "https://api.example.com/v1/"
        val originalUrl = "${DynamicApiUrlInterceptor.BASE_URL_PLACEHOLDER}test"
        val expectedUrl = "https://api.example.com/v1/test"

        testUrlReplacement(substituteUrl, originalUrl, expectedUrl)
    }

    @Test
    fun `should handle different configured URL ports`() {
        val substituteUrl = "https://api.example.com:8080/v2/"
        val originalUrl = "${DynamicApiUrlInterceptor.BASE_URL_PLACEHOLDER}endpoint"
        val expectedUrl = "https://api.example.com:8080/v2/endpoint"

        testUrlReplacement(substituteUrl, originalUrl, expectedUrl)
    }

    @Test
    fun `should handle configured URL without trailing slash`() {
        val substituteUrl = "https://api.example.com/v1"
        val originalUrl = "${DynamicApiUrlInterceptor.BASE_URL_PLACEHOLDER}health"
        val expectedUrl = "https://api.example.com/v1/health"

        testUrlReplacement(substituteUrl, originalUrl, expectedUrl)
    }

    @Test
    fun `should handle configured URL with multiple path segments`() {
        val substituteUrl = "https://api.example.com/v1/api/sevibus/"
        val originalUrl = "${DynamicApiUrlInterceptor.BASE_URL_PLACEHOLDER}lines"
        val expectedUrl = "https://api.example.com/v1/api/sevibus/lines"

        testUrlReplacement(substituteUrl, originalUrl, expectedUrl)
    }

    @Test
    fun `should handle request to root path`() {
        val substituteUrl = "https://api.example.com/v1/"
        val originalUrl = "${DynamicApiUrlInterceptor.BASE_URL_PLACEHOLDER}"
        val expectedUrl = "https://api.example.com/v1/"

        testUrlReplacement(substituteUrl, originalUrl, expectedUrl)
    }

    @Test
    fun `should preserve headers and method`() {
        val substituteUrl = "https://api.example.com/v1/"
        val originalUrl = "${DynamicApiUrlInterceptor.BASE_URL_PLACEHOLDER}data"
        val expectedUrl = "https://api.example.com/v1/data"

        // Given
        apiConfigurationManager.updateApiUrl(substituteUrl)
        val originalRequest = Request.Builder()
            .url(originalUrl)
            .header("Authorization", "Bearer token123")
            .header("Content-Type", "application/json")
            .post(okhttp3.RequestBody.create(null, "{}"))
            .build()

        val expectedHttpUrl = expectedUrl.toHttpUrl()

        whenever(mockChain.request()).thenReturn(originalRequest)
        whenever(mockChain.proceed(any<Request>())).thenReturn(mockResponse)

        // When
        interceptor.intercept(mockChain)

        // Then
        val requestCaptor = argumentCaptor<Request>()
        verify(mockChain).proceed(requestCaptor.capture())
        val capturedRequest = requestCaptor.firstValue
        expectThat(capturedRequest.url).isEqualTo(expectedHttpUrl)
        expectThat(capturedRequest.method).isEqualTo("POST")
        expectThat(capturedRequest.header("Authorization")).isEqualTo("Bearer token123")
        expectThat(capturedRequest.header("Content-Type")).isEqualTo("application/json")
    }

    @Test
    fun `should work with real AWS API Gateway URLs`() {
        val substituteUrl = "https://abc123.execute-api.eu-west-1.amazonaws.com/prod/"
        val originalUrl = "${DynamicApiUrlInterceptor.BASE_URL_PLACEHOLDER}api/stops/456"
        val expectedUrl = "https://abc123.execute-api.eu-west-1.amazonaws.com/prod/api/stops/456"

        testUrlReplacement(substituteUrl, originalUrl, expectedUrl)
    }

    @Test
    fun `should not modify request if URL does not match base URL pattern`() {
        val substituteUrl = "https://api.example.com/v1/"
        val originalUrl = "https://different.api/endpoint"
        val expectedUrl = "https://different.api/endpoint"

        testUrlReplacement(substituteUrl, originalUrl, expectedUrl)
    }

    private fun testUrlReplacement(
        substituteUrl: String,
        originalUrl: String,
        expectedUrl: String
    ) {
        // Given
        apiConfigurationManager.updateApiUrl(substituteUrl)
        val originalRequest = Request.Builder().url(originalUrl).build()
        val expectedHttpUrl = expectedUrl.toHttpUrl()

        whenever(mockChain.request()).thenReturn(originalRequest)
        whenever(mockChain.proceed(any<Request>())).thenReturn(mockResponse)

        // When
        interceptor.intercept(mockChain)

        // Then
        val requestCaptor = argumentCaptor<Request>()
        verify(mockChain).proceed(requestCaptor.capture())
        expectThat(requestCaptor.firstValue.url).isEqualTo(expectedHttpUrl)
    }
}
