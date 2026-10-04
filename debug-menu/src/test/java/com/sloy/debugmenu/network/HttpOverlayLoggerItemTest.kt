package com.sloy.debugmenu.network

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class HttpOverlayLoggerItemTest {

    @Test
    fun `redactPath replaces uuids`() {
        expectThat(redactPath("/api/users/123e4567-e89b-12d3-a456-426614174000/cards"))
            .isEqualTo("/api/users/{id}/cards")
    }

    @Test
    fun `redactPath shortens long segments`() {
        val longSegment = "a".repeat(20) + "b".repeat(21)

        expectThat(redactPath("/api/$longSegment")).isEqualTo("/api/aaa…bbb")
    }

    @Test
    fun `redactPath keeps regular paths`() {
        expectThat(redactPath("/api/stops/123/arrivals")).isEqualTo("/api/stops/123/arrivals")
    }

    @Test
    fun `badge shows the method while pending`() {
        expectThat(item().badgeLabel()).isEqualTo("GET")
    }

    @Test
    fun `badge shows the status when finished`() {
        expectThat(item(status = 404).badgeLabel()).isEqualTo("GET 404")
    }

    @Test
    fun `badge shows the error for io failures`() {
        val failed = item(status = HttpOverlayLoggerItem.STATUS_IO_EXCEPTION, error = "Unable to resolve host example.com")

        expectThat(failed.badgeLabel()).isEqualTo("GET Unable to resolve host example.com")
    }

    @Test
    fun `auto hides only once a status is known`() {
        expectThat(item().autoHide).isFalse()
        expectThat(item(status = 200).autoHide).isTrue()
    }

    @Test
    fun `badge shows the cache origin`() {
        expectThat(item(status = 200, cache = HttpOverlayLoggerItem.Cache.LOCAL).badgeLabel()).isEqualTo("GET 200 (local)")
        expectThat(item(status = 304, cache = HttpOverlayLoggerItem.Cache.NOT_MODIFIED).badgeLabel()).isEqualTo("GET 304 (not modified)")
        expectThat(item(status = 200, cache = HttpOverlayLoggerItem.Cache.MISS).badgeLabel()).isEqualTo("GET 200")
    }

    private fun item(status: Int? = null, error: String? = null, cache: HttpOverlayLoggerItem.Cache? = null) =
        HttpOverlayLoggerItem(method = "GET", endpoint = "/api/stops", id = "id", status = status, error = error, cache = cache)
}
